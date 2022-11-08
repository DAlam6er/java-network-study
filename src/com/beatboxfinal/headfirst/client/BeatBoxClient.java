package com.beatboxfinal.headfirst.client;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO:
//  1) Добавить вывод на экран диалогового окна, предлагающий пользователю
//  сохранить текущий шаблон, в случае, если он выбирает пункт из списка
//  2) Создать кнопку, генерирующую случайный шаблон.
//  3) Добавить возможность подгружать базовые шаблоны: "Джаз", "Рок, "Регги"
public class BeatBoxClient
{
    public static final String DEFAULT_USERNAME="user";

    private JFrame theFrame;

    private JList<String> incomingList;
    private JTextArea userMessage;
    private ArrayList<JCheckBox> checkBoxList;

    private final Vector<String> listVector = new Vector<>();
    HashMap<String, boolean[]> otherSeqsMap = new HashMap<>();

    private String userName;
    private int nextNum;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat",
        "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo",
        "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
        "Low-mid Tom", "High Agogo", "Open Hi Conga"};

    int[] instruments =
        {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public static void main(String[] args)
    {
        // args[0] - пользовательский идентификатор
        try {
            new BeatBoxClient().startUp(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.printf(
                "No username found in command line, " +
                    "using default username \"%s\"\n", DEFAULT_USERNAME);
            new BeatBoxClient().startUp(DEFAULT_USERNAME);
        }
    }

    public void startUp(String userName)
    {
        this.userName = userName;
        try (Socket sock = new Socket("127.0.0.1", 4242)) {
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            /*
            Thread remote = new Thread(new RemoteReader());
            remote.start();
             */
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(new RemoteReader());
        } catch (IOException e) {
            System.out.println("couldn't connect to server — " +
                    "you'll probably have to play alone.");
        }
        setUpMidi();
        buildGUI();
    }

    public void buildGUI()
    {
        theFrame = new JFrame("Cyber BeatBox");
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(
            BorderFactory.createEmptyBorder(10,10,10,10));

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(e -> buildTrackAndStart());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(e -> sequencer.stop());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(e -> changeTempo(1.03f));
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(e -> changeTempo(0.97f));
        buttonBox.add(downTempo);

        JButton serializeIt = new JButton("Save");
        serializeIt.addActionListener(e ->
        {
            JFileChooser fileSave = new JFileChooser();
            fileSave.showSaveDialog(theFrame);
            saveFile(fileSave.getSelectedFile());
        });
        buttonBox.add(serializeIt);

        JButton restore = new JButton("Load");
        restore.addActionListener(e ->
        {
            JFileChooser fileOpen = new JFileChooser();
            fileOpen.showOpenDialog(theFrame);
            loadFile(fileOpen.getSelectedFile());
        });
        buttonBox.add(restore);

        JButton sendIt = new JButton("sendIt");
        // сериализовываем 2 объекта (строковое сообщение и музыкальный шаблон)
        // и записываем их в исходящий поток сокета (на сервер).
        sendIt.addActionListener(e ->sendMessageAndTracks());
        buttonBox.add(sendIt);

        userMessage = new JTextArea();
        userMessage.setLineWrap(true);
        userMessage.setWrapStyleWord(true);
        JScrollPane messageScroller = new JScrollPane(userMessage);
        buttonBox.add(messageScroller);

        // в JList отображаются входящие сообщения,
        // которые можно выбирать из списка, а не только просматривать.
        // с его помощью будут загружаться и воспроизводиться музыкальные шаблоны
        incomingList = new JList<>();
        // Срабатывает когда пользователь выбирает сообщения из списка.
        // при этом мы сразу загружаем соответствующий музыкальный шаблон,
        // хранящийся в otherSeqsMap и указываем проигрывать его
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector); // нет начальных данных

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        /*
        nameBox.add(new Label());
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }
         */
        for (String instrumentName : instrumentNames) {
            JLabel instrumentLabel = new JLabel(instrumentName);
            // this border on each instrument name helps them line up wih the checkbox
            instrumentLabel.setBorder(
                BorderFactory.createEmptyBorder(4, 1,4,1));
            nameBox.add(instrumentLabel);
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);
        GridLayout grid = new GridLayout(17, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        JPanel mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 1 ; i < 17; i++) {
            mainPanel.add(new JLabel(String.format("%02d", i)));
        }

        checkBoxList = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        theFrame.setBounds(50,50,300,300);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setLocationRelativeTo(null);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    // Получаем объект Sequencer, создаем последовательность и трек
    public void setUpMidi()
    {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (InvalidMidiDataException | MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void changeTempo(float tempoMultiplier)
    {
        float tempoFactor = sequencer.getTempoFactor();
        sequencer.setTempoFactor(tempoFactor * tempoMultiplier);
    }

    private void sendMessageAndTracks() {
        boolean[] checkboxState = new boolean[256];
        for (int i = 0; i < 256; i++) {
            JCheckBox check = checkBoxList.get(i);
            if (check.isSelected()) {
                checkboxState[i] = true;
            }
        }
        try {
            out.writeObject(
                String.format("%s%d: %s\n",
                    userName, nextNum++, userMessage.getText()));
            out.writeObject(checkboxState);
        } catch (IOException ex) {
            System.out.println("Sorry dude. Could not sent it to the server.");
            ex.printStackTrace();
        }
        userMessage.setText("");
    }

    private void saveFile(File file)
    {
        boolean[] checkboxState = new boolean[256];
        for (int i = 0; i < 256; i++) {
            JCheckBox check = checkBoxList.get(i);
            if (check.isSelected()) {
                checkboxState[i] = true;
            }

            try (FileOutputStream fileStream = new FileOutputStream(file);
                 ObjectOutputStream os =
                     new ObjectOutputStream(fileStream))
            {
                os.writeObject(checkboxState);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadFile(File file)
    {
        boolean[] checkboxState = null;
        try (FileInputStream fileIn = new FileInputStream(file);
             ObjectInputStream is = new ObjectInputStream(fileIn))
        {
            checkboxState = (boolean[]) is.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        changeSequence(checkboxState);
        sequencer.stop();
        buildTrackAndStart();
    }

    // Создаем трек, проверяя состояние всех флажков,
    // и связывая их с инструментом (для которого создается MidiEvent).
    public void buildTrackAndStart()
    {
        ArrayList<Integer> trackList;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) {
            trackList = new ArrayList<>();
            int key = instruments[i];
            for (int j = 0; j < 16; j++) {
                JCheckBox jc = checkBoxList.get(j + 16 * i);
                if (jc.isSelected()) {
                    trackList.add(key);
                } else {
                    // этот слот в треке должен быть пустым
                    trackList.add(null);
                }
            }
            makeTracks(trackList);
            track.add(makeEvent(
                ShortMessage.CONTROL_CHANGE, 1, 127, 0, 16));
        }
        // в результате мы всегда имеем полные 16 тактов
        track.add(makeEvent(
            ShortMessage.PROGRAM_CHANGE, 9, 1, 0, 15));
        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.setTempoInBPM(120);
            sequencer.start();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    // Задача потока - читать данные, присылаемые сервером.
    // Под данными понимаются 2 сериализованных объекта:
    // строковое сообщение и музыкальная последовательность
    // (ArrayList с состоянием флажков).
    public class RemoteReader implements Runnable
    {
        @Override
        public void run()
        {
            try {
                Object obj;
                boolean[] checkboxState;
                while ((obj = in.readObject()) != null) {
                    System.out.println("got an object from server");
                    System.out.println(obj.getClass());

                    String nameToShow = (String) obj;
                    checkboxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);

                    // Vector<String> - устаревший аналог ArrayList
                    listVector.add(nameToShow);
                    // Добавляем результат (Vector) в JList
                    incomingList.setListData(listVector);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyListSelectionListener implements ListSelectionListener
    {
        @Override
        public void valueChanged(ListSelectionEvent lse)
        {
            if (!lse.getValueIsAdjusting()) {
                String selected = incomingList.getSelectedValue();
                if (selected != null) {
                    // Переходим к отображению и изменяем последовательность
                    boolean[] selectedState = otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                }
            }
        }
    }

    // IMMEDIATELY change the pattern to the selected from list
    private void changeSequence(boolean[] checkboxState)
    {
        for (int i = 0; i < 256; i++) {
            JCheckBox check = checkBoxList.get(i);
            check.setSelected(checkboxState[i]);
        }
    }

    public void makeTracks(ArrayList<Integer> list)
    {
        for (int i = 0; i < list.size(); i++) {
            Integer instrumentKey = list.get(i);
            // Создаем события включения и выключения и добавляем их в дорожку
            if (instrumentKey != null) {
                track.add(
                    makeEvent(ShortMessage.NOTE_ON, 9, instrumentKey, 100, i));
                track.add(
                    makeEvent(ShortMessage.NOTE_OFF, 9, instrumentKey, 100, i + 1));
            }
        }
    }

    public MidiEvent makeEvent(int cmd, int chnl, int one, int two, int tick)
    {
        MidiEvent event = null;
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(cmd, chnl, one, two);
            event = new MidiEvent(msg, tick);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return event;
    }
}
