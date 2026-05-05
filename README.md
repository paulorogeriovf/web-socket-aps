# 🌊 Sistema de Monitoramento — Rio Tietê

> Aplicação desktop de comunicação em tempo real para inspetores da Secretaria Estadual do Meio Ambiente, desenvolvida em Java com TCP/IP via Berkeley Sockets e interface gráfica Swing.

---

## 📋 Sobre o Projeto

O sistema permite que múltiplos inspetores ambientais se conectem simultaneamente a um servidor central para trocar mensagens, enviar arquivos e emitir alertas de diferentes níveis de prioridade — tudo em tempo real, com interface visual temática voltada ao monitoramento do Rio Tietê.

---

## ✨ Funcionalidades

- 💬 **Chat em tempo real** entre múltiplos inspetores conectados simultaneamente
- 🔴 **Níveis de alerta** configuráveis por mensagem: Normal, Alerta e Urgente
- 📁 **Transferência de arquivos** entre clientes (limite de 10 MB por arquivo)
- 👥 **Lista de inspetores online** atualizada automaticamente em tempo real
- ✍️ **Indicador "digitando..."** notifica os outros quando alguém está escrevendo
- 😊 **Atalhos de emoji** automáticos (`:)` → 😊, `<3` → ❤️, etc.)
- 🔔 **Notificações de entrada e saída** de inspetores no sistema
- 🖥️ **Launcher integrado** para iniciar servidor e abrir múltiplos clientes pela mesma interface
- 📋 **Log do servidor** com timestamps e botão para limpar

---

## 🗂️ Estrutura dos Arquivos

```
├── Launcher.java        # Ponto de entrada — gerencia servidor e clientes
├── Servidor.java        # Servidor TCP com interface de monitoramento
├── ClienteHandler.java  # Gerencia cada conexão de cliente no servidor
└── ClienteGUI.java      # Interface do inspetor (login + chat)
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17 ou superior (utiliza switch expressions com `->`)

### Compilar

```bash
javac Launcher.java Servidor.java ClienteHandler.java ClienteGUI.java
```

### Executar

```bash
java Launcher
```

O **Launcher** é o único ponto de entrada. A partir dele:

1. Clique em **▶ Iniciar Servidor** — o servidor sobe na porta `12345`
2. Clique em **＋ Abrir Cliente** quantas vezes quiser para abrir janelas de inspetores
3. Em cada janela de cliente, insira o nome do inspetor e entre no sistema

> É possível também rodar o servidor e os clientes em máquinas separadas — nesse caso, edite o endereço `localhost` em `ClienteGUI.java` para o IP do servidor.

---

## 🏗️ Arquitetura

```
┌─────────────┐        TCP/IP         ┌──────────────────┐
│  ClienteGUI │ ◄────────────────────► │                  │
├─────────────┤        porta 12345     │     Servidor     │
│  ClienteGUI │ ◄────────────────────► │                  │
├─────────────┤                        │  ClienteHandler  │
│  ClienteGUI │ ◄────────────────────► │  (1 por cliente) │
└─────────────┘                        └──────────────────┘
```

- O servidor aceita conexões e cria uma thread `ClienteHandler` para cada cliente
- Cada handler só é registrado na lista ativa **após** receber o nome do inspetor, evitando entradas fantasmas
- O broadcast de mensagens e arquivos percorre a lista de handlers registrados
- Ao fechar a janela do cliente, o socket é encerrado imediatamente e o servidor notifica os demais

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| Java Swing | Interface gráfica (Launcher, Servidor, Cliente) |
| TCP/IP Sockets | Comunicação em rede (Berkeley Sockets) |
| `DataInputStream` / `DataOutputStream` | Protocolo de mensagens binário |
| `CopyOnWriteArrayList` | Lista de clientes thread-safe |
| `SwingUtilities.invokeLater` | Atualização segura da UI a partir de threads |

---

## 📡 Protocolo de Comunicação

As mensagens trocadas entre cliente e servidor seguem um protocolo simples baseado em tipo:

| Tipo | Payload | Descrição |
|---|---|---|
| `MSG` | `String` | Mensagem de texto |
| `FILE` | `String` + `int` + `byte[]` | Nome, tamanho e bytes do arquivo |
| `USERS` | `String` | Lista de nomes separados por vírgula |
| `TYPING` | `String` | Nome de quem está digitando |

Na conexão, o cliente envia imediatamente seu nome antes de qualquer outro dado.

---

## 📸 Interface

| Launcher | Servidor | Cliente |
|---|---|---|
| Inicia servidor e abre clientes | Log de conexões em tempo real | Chat com alertas e transferência de arquivos |

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos e de demonstração técnica.