import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Launcher {

    // cores
    private static final Color BG_ESCURO      = new Color(13,  27,  20);
    private static final Color BG_PAINEL      = new Color(20,  40,  30);
    private static final Color VERDE_PRIMARIO = new Color(52,  199, 120);
    private static final Color VERDE_CLARO    = new Color(120, 230, 160);
    private static final Color TEXTO_CLARO    = new Color(220, 240, 228);
    private static final Color TEXTO_APAGADO  = new Color(100, 140, 115);
    private static final Color ALERTA_DANGER  = new Color(255,  80,  80);

    // estados
    private static boolean servidorRodando = false;
    private static int clientesAbertos = 0;
    private static JLabel statusServidor;
    private static JLabel contadorClientes;
    private static JButton btnServidor;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(Launcher::criarJanela);
    }

    private static void criarJanela() {
        JFrame frame = new JFrame("Sistema Ambiental — Launcher");
        frame.setSize(480, 620);
        frame.setMinimumSize(new Dimension(480, 580));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);

        JPanel content = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 25, 16), 0, getHeight(), new Color(18, 48, 30));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setOpaque(true);

        content.add(criarCabecalho(),  BorderLayout.NORTH);
        content.add(criarCentro(),     BorderLayout.CENTER);
        content.add(criarRodape(),     BorderLayout.SOUTH);

        frame.setContentPane(content);
        frame.setVisible(true);
    }

    // cabecalho

    private static JPanel criarCabecalho() {
        JPanel painel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(12, 35, 22));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(VERDE_PRIMARIO);
                g.fillRect(0, getHeight() - 2, getWidth(), 2);
            }
        };
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(18, 0, 14, 0));

        JLabel icone = new JLabel("🌊", SwingConstants.CENTER);
        icone.setFont(new Font("Dialog", Font.PLAIN, 36));
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("SISTEMA DE MONITORAMENTO");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 16));
        titulo.setForeground(VERDE_PRIMARIO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Rio Tietê · Secretaria Estadual do Meio Ambiente");
        sub.setFont(new Font("Dialog", Font.PLAIN, 11));
        sub.setForeground(TEXTO_APAGADO);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        painel.add(icone);
        painel.add(Box.createVerticalStrut(10));
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(5));
        painel.add(sub);

        return painel;
    }

    // centro laucher

    private static JPanel criarCentro() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(16, 48, 14, 48));

        // ── Card Servidor ────────────────────────────────────────
        JPanel cardServidor = criarCard(
            "⚙️  Servidor",
            "Inicia o servidor central que\ngerencia todas as conexões.",
            VERDE_PRIMARIO
        );

        btnServidor = criarBotao("▶  Iniciar Servidor", VERDE_PRIMARIO, new Color(10, 30, 18));
        btnServidor.addActionListener(e -> toggleServidor());
        cardServidor.add(Box.createVerticalStrut(14));
        cardServidor.add(envolverCentro(btnServidor));

        statusServidor = new JLabel("● Servidor offline");
        statusServidor.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusServidor.setForeground(ALERTA_DANGER);
        statusServidor.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardServidor.add(Box.createVerticalStrut(8));
        cardServidor.add(envolverCentro(statusServidor));

        // divisor
        JPanel divisor = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(30, 65, 45));
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        divisor.setOpaque(false);
        divisor.setPreferredSize(new Dimension(0, 24));
        divisor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        // Card do cliente
        JPanel cardCliente = criarCard(
            "👤  Cliente",
            "Abre uma nova janela de inspetor.\nPode abrir quantas quiser.",
            VERDE_CLARO
        );

        JButton btnCliente = criarBotao("＋  Abrir Cliente", new Color(30, 90, 55), VERDE_CLARO);
        btnCliente.addActionListener(e -> abrirCliente());
        cardCliente.add(Box.createVerticalStrut(14));
        cardCliente.add(envolverCentro(btnCliente));

        contadorClientes = new JLabel("0 cliente(s) aberto(s)");
        contadorClientes.setFont(new Font("Monospaced", Font.PLAIN, 11));
        contadorClientes.setForeground(TEXTO_APAGADO);
        contadorClientes.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardCliente.add(Box.createVerticalStrut(8));
        cardCliente.add(envolverCentro(contadorClientes));

        painel.add(cardServidor);
        painel.add(Box.createVerticalStrut(6));
        painel.add(divisor);
        painel.add(Box.createVerticalStrut(6));
        painel.add(cardCliente);

        return painel;
    }

    // rodape

    private static JPanel criarRodape() {
        JPanel painel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(12, 30, 20));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(28, 65, 42));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        painel.setOpaque(false);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel info = new JLabel("TCP/IP · Berkeley Sockets · Java Swing");
        info.setFont(new Font("Monospaced", Font.PLAIN, 10));
        info.setForeground(new Color(45, 85, 60));

        JLabel versao = new JLabel("v2.0");
        versao.setFont(new Font("Monospaced", Font.PLAIN, 10));
        versao.setForeground(new Color(45, 85, 60));

        painel.add(info,   BorderLayout.WEST);
        painel.add(versao, BorderLayout.EAST);
        return painel;
    }

    // acoes dos botoes

    private static void toggleServidor() {
        if (!servidorRodando) {
            // subir o servidor na mesma JVM em thread separada
            Thread t = new Thread(() -> {
                try {
                    Servidor.main(new String[]{});
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        statusServidor.setText("● Erro ao iniciar servidor");
                        statusServidor.setForeground(ALERTA_DANGER);
                    });
                }
            });
            t.setDaemon(true);
            t.start();

            // Aguarda e verifica se subiu
            new javax.swing.Timer(600, e -> {
                try {
                    // testa  a conexão 
                    Socket teste = new Socket("localhost", 12345);
                    teste.close();
                    servidorRodando = true;
                    statusServidor.setText("● Servidor online — porta 12345");
                    statusServidor.setForeground(VERDE_PRIMARIO);
                    btnServidor.repaint();
                } catch (Exception ex) {
                    statusServidor.setText("● Aguardando servidor...");
                    statusServidor.setForeground(ALERTA_DANGER);
                }
                ((javax.swing.Timer) e.getSource()).stop();
            }).start();

            btnServidor.putClientProperty("online", true);
            btnServidor.repaint();

        } else {
            JOptionPane.showMessageDialog(null,
                "O servidor está em execução.\nFeche o Launcher para encerrá-lo.",
                "Servidor ativo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void abrirCliente() {
        if (!servidorRodando) {
            // Tenta detectar servidor externo antes de barrar
            try {
                Socket teste = new Socket("localhost", 12345);
                teste.close();
                servidorRodando = true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Inicie o servidor antes de abrir um cliente.",
                    "Servidor offline", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        clientesAbertos++;
        contadorClientes.setText(clientesAbertos + " cliente(s) aberto(s)");

        // Chamar mostrarLogin do ClienteGUI para abrir a janela do cliente
        ClienteGUI.mostrarLogin();
    }

    // componentes 

    private static JPanel criarCard(String titulo, String descricao, Color corDestaque) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_PAINEL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(corDestaque);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        labelTitulo.setForeground(corDestaque);
        labelTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // múltiplas linhas na descrição
        for (String linha : descricao.split("\n")) {
            JLabel l = new JLabel(linha);
            l.setFont(new Font("Dialog", Font.PLAIN, 11));
            l.setForeground(TEXTO_APAGADO);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (linha.equals(descricao.split("\n")[0])) {
                card.add(labelTitulo);
                card.add(Box.createVerticalStrut(6));
            }
            card.add(l);
        }

        return card;
    }

    private static JButton criarBotao(String texto, Color corFundo, Color corTexto) {
        JButton btn = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()  ? corFundo.darker()
                         : getModel().isRollover() ? corFundo.brighter()
                         : corFundo;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(corTexto);
                g2.setFont(new Font("Dialog", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static JPanel envolverCentro(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(comp);
        return p;
    }
}