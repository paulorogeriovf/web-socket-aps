import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.*;
import java.awt.*;

public class Servidor {

    private static final java.util.List<ClienteHandler> clientes = new CopyOnWriteArrayList<>();
    private static JTextArea logArea;
    private static JLabel statusLabel;
    private static int totalConexoes = 0;

    public static void main(String[] args) {
        criarInterfaceServidor();

        new Thread(() -> {
            try (ServerSocket servidor = new ServerSocket(12345)) {
                log("✅ Servidor iniciado na porta 12345");
                log("⏳ Aguardando conexões...");

                while (true) {
                    Socket socket = servidor.accept();
                    totalConexoes++;
                    log("🔌 Nova conexão recebida: " + socket.getInetAddress().getHostAddress());

                    ClienteHandler handler = new ClienteHandler(socket);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                log("❌ Erro no servidor: " + e.getMessage());
            }
        }).start();
    }

    private static void criarInterfaceServidor() {
        JFrame frame = new JFrame("🌿 Servidor — Monitoramento Rio Tietê");
        frame.setSize(600, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // painel de topo
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(new Color(15, 40, 30));
        topo.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel titulo = new JLabel("⚙️  SERVIDOR — SECRETARIA AMBIENTAL");
        titulo.setFont(new Font("Monospaced", Font.BOLD, 14));
        titulo.setForeground(new Color(80, 220, 140));

        statusLabel = new JLabel("● 0 clientes conectados");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 200, 120));

        topo.add(titulo, BorderLayout.WEST);
        topo.add(statusLabel, BorderLayout.EAST);

        // log
        logArea = new JTextArea();
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBackground(new Color(10, 25, 18));
        logArea.setForeground(new Color(80, 220, 140));
        logArea.setCaretColor(new Color(80, 220, 140));
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(30, 80, 50), 1));

        // rodape
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(new Color(15, 40, 30));
        rodape.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel info = new JLabel("TCP/IP · Porta 12345 · Berkeley Sockets");
        info.setFont(new Font("Monospaced", Font.PLAIN, 11));
        info.setForeground(new Color(60, 140, 90));

        JButton limpar = new JButton("Limpar Log");
        limpar.setBackground(new Color(30, 80, 50));
        limpar.setForeground(new Color(80, 220, 140));
        limpar.setFocusPainted(false);
        limpar.setBorderPainted(false);
        limpar.setFont(new Font("Monospaced", Font.PLAIN, 11));
        limpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        limpar.addActionListener(e -> logArea.setText(""));

        rodape.add(info, BorderLayout.WEST);
        rodape.add(limpar, BorderLayout.EAST);

        frame.add(topo, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(rodape, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void log(String msg) {
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String linha = "[" + hora + "] " + msg + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(linha);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        System.out.print(linha);
    }

    private static void atualizarStatus() {
        SwingUtilities.invokeLater(() ->
            statusLabel.setText("● " + clientes.size() + " online | histórico total: " + totalConexoes)
        );
    }

    // registra um novo cliente na lista e atualiza o status

    public static void registrarCliente(ClienteHandler c) {
        clientes.add(c);
        atualizarStatus();
    }

    // broadcast de mensagens para todos os clientes, exceto o remetente (para evitar ecos)

    public static void broadcastMensagem(String msg, ClienteHandler remetente) {
        log("💬 " + msg);
        for (ClienteHandler c : clientes) {
            if (c != remetente) c.enviarMensagem(msg);
        }
    }

    public static void broadcastArquivo(String nomeArquivo, byte[] dados, ClienteHandler remetente) {
        log("📁 Arquivo transmitido: " + nomeArquivo + " (" + dados.length / 1024 + " KB)");
        for (ClienteHandler c : clientes) {
            if (c != remetente) c.enviarArquivo(nomeArquivo, dados);
        }
    }

    public static void broadcastListaUsuarios() {
        StringBuilder sb = new StringBuilder();
        for (ClienteHandler c : clientes) {
            sb.append(c.getNome()).append(",");
        }
        String lista = sb.toString();
        for (ClienteHandler c : clientes) {
            c.enviarListaUsuarios(lista);
        }
    }

    public static void removerCliente(ClienteHandler c) {
        clientes.remove(c);
        log("🔴 " + c.getNome() + " desconectou. Clientes ativos: " + clientes.size());
        atualizarStatus();
        broadcastListaUsuarios();
    }
}