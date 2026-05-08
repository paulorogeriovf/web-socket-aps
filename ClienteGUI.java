import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClienteGUI extends JFrame {

    // Cores
    private static final Color BG_ESCURO      = new Color(13,  27,  20);
    private static final Color BG_PAINEL      = new Color(20,  40,  30);
    private static final Color VERDE_PRIMARIO = new Color(52,  199, 120);
    private static final Color VERDE_CLARO    = new Color(120, 230, 160);
    private static final Color TEXTO_CLARO    = new Color(220, 240, 228);
    private static final Color TEXTO_APAGADO  = new Color(100, 140, 115);
    private static final Color ALERTA_WARN    = new Color(255, 200,  60);
    private static final Color ALERTA_DANGER  = new Color(255,  80,  80);
    private static final Color BUBBLE_PROPRIO = new Color(30,   90,  55);
    private static final Color BUBBLE_OUTRO   = new Color(25,   55,  40);

    // Componentes
    private JTextPane chat;
    private JTextField campo;
    private JButton btnEnviar, btnArquivo;
    private JPanel painelUsuarios;
    private JLabel labelDigitando;
    private JComboBox<String> comboAlerta;

    // rede
    private DataInputStream in;
    private DataOutputStream out;
    private String nome;
    private Socket socket;


    private javax.swing.Timer timerDigitando;

    public static void main(String[] args) {
        // CrossPlatform = Metal, que respeita cores customizadas
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        mostrarLogin();
    }

    // Login via JDialog modal 
    public static void mostrarLogin() {
        JDialog dialog = new JDialog((Frame) null, "Acesso — Secretaria Ambiental", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final String[] nomeFinal = {null};

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 30, 20), 0, getHeight(), new Color(20, 55, 35));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);

        // Topo
        JPanel topo = new JPanel();
        topo.setLayout(new BoxLayout(topo, BoxLayout.Y_AXIS));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(28, 32, 12, 32));

        JLabel icone = new JLabel("🌊");
        icone.setFont(new Font("Dialog", Font.PLAIN, 32));
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("SECRETARIA AMBIENTAL");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(new Color(52, 199, 120));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sistema de Monitoramento · Rio Tietê");
        sub.setFont(new Font("Dialog", Font.PLAIN, 11));
        sub.setForeground(new Color(100, 140, 115));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        topo.add(icone);
        topo.add(Box.createVerticalStrut(6));
        topo.add(titulo);
        topo.add(Box.createVerticalStrut(3));
        topo.add(sub);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(8, 32, 28, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel labelNome = new JLabel("Identificação do Inspetor");
        labelNome.setFont(new Font("Dialog", Font.PLAIN, 11));
        labelNome.setForeground(new Color(100, 140, 115));

        JTextField nomeField = new JTextField();
        nomeField.setFont(new Font("Dialog", Font.PLAIN, 14));
        nomeField.setBackground(new Color(30, 60, 45));
        nomeField.setForeground(new Color(220, 240, 228));
        nomeField.setCaretColor(new Color(52, 199, 120));
        nomeField.setOpaque(true);
        nomeField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 199, 120), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JButton btnEntrar = new JButton("Entrar no Sistema") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(35, 150, 85)
                         : getModel().isRollover() ? new Color(70, 220, 140)
                         : new Color(52, 199, 120);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(10, 30, 18));
                g2.setFont(new Font("Dialog", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnEntrar.setPreferredSize(new Dimension(160, 38));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setContentAreaFilled(false);
        btnEntrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 0);
        form.add(labelNome, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 12, 0);
        form.add(nomeField, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        form.add(btnEntrar, gbc);

        root.add(topo, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        dialog.setContentPane(root);

        Runnable confirmar = () -> {
            String txt = nomeField.getText().trim();
            if (!txt.isEmpty()) {
                nomeFinal[0] = txt;
                dialog.dispose();
            } else {
                nomeField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 80, 80), 1),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        };

        btnEntrar.addActionListener(e -> confirmar.run());
        nomeField.addActionListener(e -> confirmar.run());

        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (nomeFinal[0] != null) {
                    String n = nomeFinal[0];
                    SwingUtilities.invokeLater(() -> new ClienteGUI(n));
                } else {
                    System.exit(0);
                }
            }
        });

        dialog.setVisible(true); // bloqueia corretamente (modal)
    }

    // contrutor
    public ClienteGUI(String nome) {
        this.nome = nome;
        construirTela();
        conectar();
    }

    // interface

    private void construirTela() {
        setTitle("Monitoramento Ambiental · " + nome);
        setSize(920, 620);
        setMinimumSize(new Dimension(700, 480));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ao fechar a janela, encerra o socket — servidor detecta e notifica os outros
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                try { if (socket != null) socket.close(); } catch (Exception ignored) {}
            }
        });

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(BG_ESCURO);
        setContentPane(content);

        content.add(criarTopo(),         BorderLayout.NORTH);
        content.add(criarAreaChat(),     BorderLayout.CENTER);
        content.add(criarBarraLateral(), BorderLayout.EAST);
        content.add(criarRodape(),       BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel criarTopo() {
        JPanel topo = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(15, 45, 28));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(52, 199, 120));
                g.fillRect(0, getHeight() - 2, getWidth(), 2);
            }
        };
        topo.setPreferredSize(new Dimension(0, 54));
        topo.setOpaque(false);
        topo.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        JLabel titulo = new JLabel("🌊  Monitoramento do Rio Tietê — Salesópolis → Grande SP");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 13));
        titulo.setForeground(VERDE_CLARO);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(VERDE_PRIMARIO);
        JLabel online = new JLabel("Online");
        online.setFont(new Font("Dialog", Font.PLAIN, 11));
        online.setForeground(VERDE_PRIMARIO);
        JLabel usuario = new JLabel("👤 " + nome);
        usuario.setFont(new Font("Dialog", Font.PLAIN, 12));
        usuario.setForeground(TEXTO_APAGADO);
        direita.add(dot);
        direita.add(online);
        direita.add(Box.createHorizontalStrut(8));
        direita.add(usuario);

        topo.add(titulo, BorderLayout.WEST);
        topo.add(direita, BorderLayout.EAST);
        return topo;
    }

    private JPanel criarAreaChat() {
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBackground(BG_ESCURO);

        chat = new JTextPane();
        chat.setContentType("text/html");
        chat.setEditable(false);
        chat.setBackground(BG_ESCURO);
        chat.setOpaque(true);
        chat.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        chat.setText("<html><body style='font-family:Dialog,sans-serif; margin:0; padding:4px; background:"
            + hex(BG_ESCURO) + ";'></body></html>");

        chat.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try { Desktop.getDesktop().open(new File(e.getURL().toURI())); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        JScrollPane scroll = new JScrollPane(chat);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG_ESCURO);
        scroll.setBackground(BG_ESCURO);

        labelDigitando = new JLabel(" ");
        labelDigitando.setFont(new Font("Dialog", Font.ITALIC, 11));
        labelDigitando.setForeground(TEXTO_APAGADO);
        labelDigitando.setBackground(BG_ESCURO);
        labelDigitando.setOpaque(true);
        labelDigitando.setBorder(BorderFactory.createEmptyBorder(2, 16, 4, 16));

        centro.add(scroll,          BorderLayout.CENTER);
        centro.add(labelDigitando,  BorderLayout.SOUTH);
        return centro;
    }

    private JPanel criarBarraLateral() {
        JPanel lateral = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PAINEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(30, 70, 48));
                g.fillRect(0, 0, 1, getHeight());
            }
        };
        lateral.setPreferredSize(new Dimension(185, 0));
        lateral.setOpaque(false);

        JLabel cabecalho = new JLabel("INSPETORES ONLINE");
        cabecalho.setFont(new Font("Monospaced", Font.BOLD, 10));
        cabecalho.setForeground(VERDE_PRIMARIO);
        cabecalho.setBackground(BG_PAINEL);
        cabecalho.setOpaque(true);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));

        painelUsuarios = new JPanel();
        painelUsuarios.setLayout(new BoxLayout(painelUsuarios, BoxLayout.Y_AXIS));
        painelUsuarios.setBackground(BG_PAINEL);
        painelUsuarios.setOpaque(true);
        painelUsuarios.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JScrollPane scrollU = new JScrollPane(painelUsuarios);
        scrollU.setBorder(BorderFactory.createEmptyBorder());
        scrollU.getViewport().setBackground(BG_PAINEL);
        scrollU.setBackground(BG_PAINEL);

        JLabel versao = new JLabel("v2.0 · TCP/IP");
        versao.setFont(new Font("Monospaced", Font.PLAIN, 10));
        versao.setForeground(new Color(50, 90, 65));
        versao.setBackground(BG_PAINEL);
        versao.setOpaque(true);
        versao.setBorder(BorderFactory.createEmptyBorder(8, 14, 10, 14));

        lateral.add(cabecalho, BorderLayout.NORTH);
        lateral.add(scrollU,   BorderLayout.CENTER);
        lateral.add(versao,    BorderLayout.SOUTH);
        return lateral;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout(6, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(BG_PAINEL);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(30, 70, 48));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        comboAlerta = new JComboBox<>(new String[]{"NORMAL", "ALERTA", "URGENTE"});
        comboAlerta.setFont(new Font("Monospaced", Font.BOLD, 11));
        comboAlerta.setBackground(new Color(25, 55, 38));
        comboAlerta.setForeground(VERDE_PRIMARIO);
        comboAlerta.setOpaque(true);
        comboAlerta.setPreferredSize(new Dimension(110, 36));

        campo = new JTextField();
        campo.setFont(new Font("Dialog", Font.PLAIN, 14));
        campo.setBackground(new Color(22, 48, 34));
        campo.setForeground(TEXTO_CLARO);
        campo.setCaretColor(VERDE_PRIMARIO);
        campo.setOpaque(true);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(35, 80, 52), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { notificarDigitando(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        btnArquivo = new JButton("📁") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(35, 80, 55) : new Color(25, 55, 38));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(TEXTO_CLARO);
                g2.setFont(new Font("Dialog", Font.PLAIN, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnArquivo.setPreferredSize(new Dimension(38, 36));
        btnArquivo.setFocusPainted(false);
        btnArquivo.setBorderPainted(false);
        btnArquivo.setContentAreaFilled(false);
        btnArquivo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnEnviar = new JButton("Enviar") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? new Color(35, 150, 85)
                         : getModel().isRollover() ? new Color(70, 220, 140)
                         : VERDE_PRIMARIO;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(10, 30, 18));
                g2.setFont(new Font("Dialog", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btnEnviar.setPreferredSize(new Dimension(88, 36));
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        direita.setOpaque(false);
        direita.add(btnArquivo);
        direita.add(btnEnviar);

        rodape.add(comboAlerta, BorderLayout.WEST);
        rodape.add(campo,       BorderLayout.CENTER);
        rodape.add(direita,     BorderLayout.EAST);

        btnEnviar.addActionListener(e -> enviarMsg());
        campo.addActionListener(e -> enviarMsg());
        btnArquivo.addActionListener(e -> enviarArquivo());

        return rodape;
    }

    // conexão e comunicacao

    private void conectar() {
        try {
            socket = new Socket("localhost", 12345);
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(nome);

            new Thread(() -> {
                try {
                    while (true) {
                        String tipo = in.readUTF();
                        switch (tipo) {
                            case "MSG"    -> adicionarMensagem(in.readUTF(), false);
                            case "FILE"   -> receberArquivo();
                            case "USERS"  -> atualizarUsuarios(in.readUTF());
                            case "TYPING" -> mostrarDigitando(in.readUTF());
                        }
                    }
                } catch (Exception e) {
                    adicionarMensagem("⚠️ Conexão encerrada.", false);
                }
            }).start();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Não foi possível conectar.\nInicie o Servidor primeiro.",
                "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    // acoes

    private void enviarMsg() {
        try {
            String msg = campo.getText().trim();
            if (msg.isEmpty()) return;

            msg = msg.replace(":)", "😊").replace(":D", "😄")
                     .replace(":(", "😢").replace("<3", "❤️");

            String nivel = (String) comboAlerta.getSelectedItem();
            String prefixo = switch (nivel) {
                case "URGENTE" -> "🔴 [URGENTE] ";
                case "ALERTA"  -> "🟡 [ALERTA] ";
                default        -> "";
            };

            String msgFinal = prefixo + msg;
            out.writeUTF("MSG");
            out.writeUTF(msgFinal);

            adicionarMensagem("[" + nome + "]: " + msgFinal, true);
            campo.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enviarArquivo() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (file.length() > 10 * 1024 * 1024) {
            JOptionPane.showMessageDialog(this, "Arquivo muito grande! Máximo: 10 MB.");
            return;
        }

        try {
            byte[] dados = new FileInputStream(file).readAllBytes();
            out.writeUTF("FILE");
            out.writeUTF(file.getName());
            out.writeInt(dados.length);
            out.write(dados);
            adicionarMensagem("📁 Você enviou: " + file.getName(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receberArquivo() throws IOException {
        String nomeArq  = in.readUTF();
        int    tamanho  = in.readInt();
        byte[] dados    = new byte[tamanho];
        in.readFully(dados);

        String ts      = new SimpleDateFormat("HHmmss").format(new Date());
        String caminho = "recebido_" + ts + "_" + nomeArq;

        try (FileOutputStream fos = new FileOutputStream(caminho)) { fos.write(dados); }

        SwingUtilities.invokeLater(() -> {
            try {
                String abs  = new File(caminho).getAbsolutePath().replace("\\", "/");
                String html = "<div style='margin:6px 10px; padding:10px 14px;"
                    + " background:" + hex(new Color(20, 50, 35)) + ";"
                    + " border-left:3px solid " + hex(VERDE_PRIMARIO) + ";'>"
                    + "📁 <a href='file:///" + abs + "' style='color:" + hex(VERDE_CLARO) + ";'>"
                    + escHtml(nomeArq) + "</a>"
                    + " <span style='color:" + hex(TEXTO_APAGADO) + "; font-size:11px;'>(clique para abrir)</span></div>";
                inserirHtml(html);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private void notificarDigitando() {
        try { out.writeUTF("TYPING"); out.writeUTF(nome); } catch (Exception ignored) {}
        if (timerDigitando != null) timerDigitando.stop();
        timerDigitando = new javax.swing.Timer(2000, e -> {});
        timerDigitando.setRepeats(false);
        timerDigitando.start();
    }

    // mensagens

    private void adicionarMensagem(String msg, boolean proprio) {
        SwingUtilities.invokeLater(() -> {
            // Mensagem de sistema
            if (msg.startsWith("🔔") || msg.startsWith("🔕") || msg.startsWith("⚠️")) {
                inserirHtml("<div style='text-align:center; margin:4px 0; color:" + hex(TEXTO_APAGADO)
                    + "; font-size:11px; font-style:italic;'>" + escHtml(msg) + "</div>");
                return;
            }

            String hora = new SimpleDateFormat("HH:mm").format(new Date());

            String corFundo, corTexto = hex(TEXTO_CLARO);
            if (msg.contains("URGENTE")) {
                corFundo = hex(new Color(70, 15, 15)); corTexto = hex(ALERTA_DANGER);
            } else if (msg.contains("ALERTA")) {
                corFundo = hex(new Color(65, 50, 5));  corTexto = hex(ALERTA_WARN);
            } else {
                corFundo = proprio ? hex(BUBBLE_PROPRIO) : hex(BUBBLE_OUTRO);
            }

            String inicial;
            if (proprio) {
                inicial = nome.substring(0, 1).toUpperCase();
            } else {
                int i = msg.indexOf('['), f = msg.indexOf(']');
                inicial = (i >= 0 && f > i + 1) ? msg.substring(i + 1, i + 2).toUpperCase() : "?";
            }

            String avatarCor = proprio ? hex(new Color(40, 130, 80)) : hex(new Color(30, 90, 65));
            String raio      = proprio ? "14px 4px 14px 14px" : "4px 14px 14px 14px";

            String avatar = "<div style='width:28px;height:28px;min-width:28px;border-radius:50%;"
                + "background:" + avatarCor + ";color:white;font-size:12px;font-weight:bold;"
                + "text-align:center;line-height:28px;'>" + inicial + "</div>";

            String balao = "<div style='background:" + corFundo + ";color:" + corTexto + ";"
                + "padding:9px 13px;border-radius:" + raio + ";font-size:13px;word-wrap:break-word;'>"
                + escHtml(msg)
                + "<div style='color:" + hex(TEXTO_APAGADO) + ";font-size:10px;text-align:right;margin-top:4px;'>"
                + hora + "</div></div>";

            String html;
            if (proprio) {
                html = "<table style='margin:4px 0 4px auto; border-spacing:0;' cellpadding='0'><tr valign='bottom'>"
                     + "<td>" + balao + "</td><td style='padding-left:6px;'>" + avatar + "</td></tr></table>";
            } else {
                html = "<table style='margin:4px auto 4px 0; border-spacing:0;' cellpadding='0'><tr valign='bottom'>"
                     + "<td style='padding-right:6px;'>" + avatar + "</td><td>" + balao + "</td></tr></table>";
            }
            inserirHtml(html);
        });
    }

    private void inserirHtml(String html) {
        try {
            HTMLEditorKit kit = (HTMLEditorKit) chat.getEditorKit();
            HTMLDocument  doc = (HTMLDocument)  chat.getDocument();
            kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
            chat.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // usuarios

    private void atualizarUsuarios(String lista) {
        SwingUtilities.invokeLater(() -> {
            painelUsuarios.removeAll();
            for (String u : lista.split(",")) {
                if (u.trim().isEmpty()) continue;
                painelUsuarios.add(criarItemUsuario(u.trim()));
                painelUsuarios.add(Box.createVerticalStrut(5));
            }
            painelUsuarios.revalidate();
            painelUsuarios.repaint();
        });
    }

    private JPanel criarItemUsuario(String nomeU) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(28, 60, 42));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(170, 38));

        JLabel dot = new JLabel("●");
        dot.setForeground(VERDE_PRIMARIO);
        dot.setFont(new Font("Dialog", Font.PLAIN, 9));

        JLabel avatar = new JLabel(nomeU.substring(0, 1).toUpperCase(), SwingConstants.CENTER);
        avatar.setFont(new Font("Dialog", Font.BOLD, 11));
        avatar.setForeground(Color.WHITE);
        avatar.setBackground(nomeU.equals(nome) ? new Color(40, 130, 80) : new Color(30, 90, 65));
        avatar.setOpaque(true);
        avatar.setPreferredSize(new Dimension(22, 22));

        JLabel labelNome = new JLabel(nomeU.length() > 13 ? nomeU.substring(0, 11) + "…" : nomeU);
        labelNome.setFont(new Font("Dialog", Font.PLAIN, 12));
        labelNome.setForeground(nomeU.equals(nome) ? VERDE_PRIMARIO : TEXTO_CLARO);

        item.add(dot);
        item.add(avatar);
        item.add(labelNome);
        return item;
    }

    private void mostrarDigitando(String quem) {
        SwingUtilities.invokeLater(() -> {
            labelDigitando.setText(quem + " está digitando...");
            javax.swing.Timer t = new javax.swing.Timer(2500, e -> labelDigitando.setText(" "));
            t.setRepeats(false);
            t.start();
        });
    }

    // utilitarios

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}