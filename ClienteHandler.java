import java.io.*;
import java.net.*;

public class ClienteHandler implements Runnable {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nome;

    public ClienteHandler(Socket socket) {
        this.socket = socket;
    }

    public String getNome() {
        return nome != null ? nome : "Desconhecido";
    }

    public void enviarMensagem(String msg) {
        try {
            out.writeUTF("MSG");
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarArquivo(String nomeArquivo, byte[] dados) {
        try {
            out.writeUTF("FILE");
            out.writeUTF(nomeArquivo);
            out.writeInt(dados.length);
            out.write(dados);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarListaUsuarios(String lista) {
        try {
            out.writeUTF("USERS");
            out.writeUTF(lista);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarDigitando(String nomeDigitando) {
        try {
            out.writeUTF("TYPING");
            out.writeUTF(nomeDigitando);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Timeout de 2s para receber o nome
            // Rejeita silenciosamente conexoes de teste (ex: Launcher verificando se o servidor subiu)
            socket.setSoTimeout(2000);
            in  = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            nome = in.readUTF();

            // Desliga o timeout apos identificacao — comunicacao normal pode demorar
            socket.setSoTimeout(0);

            // So agora entra na lista oficial de clientes
            Servidor.registrarCliente(this);

            Servidor.log("👤 Inspetor conectado: " + nome);
            Servidor.broadcastMensagem("🔔 " + nome + " entrou no sistema.", this);
            Servidor.broadcastListaUsuarios();

            while (true) {
                String tipo = in.readUTF();

                switch (tipo) {
                    case "MSG" -> {
                        String msg = in.readUTF();
                        Servidor.broadcastMensagem("[" + nome + "]: " + msg, this);
                    }
                    case "FILE" -> {
                        String nomeArquivo = in.readUTF();
                        int tamanho = in.readInt();
                        byte[] dados = new byte[tamanho];
                        in.readFully(dados);
                        Servidor.broadcastArquivo(nomeArquivo, dados, this);
                    }
                    case "TYPING" -> {
                        for (ClienteHandler outro : getOutros()) {
                            outro.enviarDigitando(nome);
                        }
                    }
                }
            }

        } catch (Exception e) {
            // Conexao encerrada ou timeout de identificacao — silencioso
        } finally {
            // So loga e notifica se o cliente chegou a se identificar
            if (nome != null) {
                Servidor.broadcastMensagem("🔕 " + nome + " saiu do sistema.", this);
                Servidor.removerCliente(this);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private java.util.List<ClienteHandler> getOutros() {
        java.util.List<ClienteHandler> lista = new java.util.ArrayList<>();
        return lista;
    }
}