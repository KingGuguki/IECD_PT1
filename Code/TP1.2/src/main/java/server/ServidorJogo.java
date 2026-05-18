package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import user.User;


class ServidorDedicado extends Thread {

    // **Atributos:**
	
	// Tempo máximo para jogar
	final int timeout = 1000*30;  // 0 - sem timeout

    /**
     * Sessão da ligação com o jogador X.
     */
    private Servidor.SessaoJogador jogadorX = null; 

    /**
     * Sessão da ligação com o jogador O.
     */
    private Servidor.SessaoJogador jogadorO = null; 

    // **Construtor:**

    /**
     * Manipula as sessões virtuais dos jogadores.
     *
     * @param sessao1 Sessão do jogador X.
     * @param sessao2 Sessão do jogador O.
     */
    public ServidorDedicado(Servidor.SessaoJogador sessao1, Servidor.SessaoJogador sessao2) {
        this.jogadorX = sessao1;
        this.jogadorO = sessao2;
    }

    /**
     * Método executado pela thread para gerir um jogo.
     */
    public void run() {
        
        // Regista o tempo inicial para calcular a duração do jogo
        long tempoInicio = System.currentTimeMillis();

        // Extrai os sockets das sessões para manter a lógica original de comunicação
        Socket connectionX = jogadorX.socket;
        Socket connectionO = jogadorO.socket;

        try (
            // Cria streams para leitura e escrita de dados nos sockets
        	
        	// **Socket X:**
            BufferedReader isX = new BufferedReader(new InputStreamReader(connectionX.getInputStream()));
            PrintWriter osX = new PrintWriter(connectionX.getOutputStream(), true);
        	
        	// **Socket O:**
            BufferedReader isO = new BufferedReader(new InputStreamReader(connectionO.getInputStream()));
            PrintWriter osO = new PrintWriter(connectionO.getOutputStream(), true);
        ) {
        	// Define timeout para inatvidade
        	connectionX.setSoTimeout(timeout);
        	connectionO.setSoTimeout(timeout);
        	
            // **Informação sobre a thread**
            System.out.println("Iniciou a Thread ("+ this.threadId()+") do servidor dedicado:");

            // **Criação do jogo**
            JogoXML jogo = new JogoXML();
            char turnoAtual = 'X';

            // Ciclo para gerir a interação entre jogadores suportando a jogada Bónus
            for (;;) 
            {
                if (turnoAtual == 'X') 
                {
                    Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
                    
                    if (!jogo.terminou()) 
                    {
                        jogo = Skeleton.runJogar(isX, osX, 'X', connectionX, jogo);
                        
                        // Se não for jogada bónus (BN) nem inválida (IV), passa a vez
                        if (jogo.getEstado().equals("ND")) 
                        {
                            turnoAtual = 'O';
                        }
                    } 
                    else 
                    {
                        Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                        break;
                    }
                } 
                else 
                {
                    Skeleton.runObter(isO, osO, 'O', connectionO, jogo);
                    
                    if (!jogo.terminou()) 
                    {
                        jogo = Skeleton.runJogar(isO, osO, 'O', connectionO, jogo);
                        
                        // Se não for jogada bónus (BN) nem inválida (IV), passa a vez
                        if (jogo.getEstado().equals("ND")) 
                        {
                            turnoAtual = 'X';
                        }
                    } 
                    else 
                    {
                        Skeleton.runObter(isX, osX, 'X', connectionX, jogo);
                        break;
                    }
                }
            }
            
            // --- CÓDIGO ADICIONADO PARA ESTATÍSTICAS ---
            
            long tempoFim = System.currentTimeMillis();
            int tempoDecorridoSecs = (int) ((tempoFim - tempoInicio) / 1000);

            // Obtém o estado final diretamente do jogo para decidir o vencedor
            String estadoFinal = jogo.getEstado();
            boolean vitX = estadoFinal.equals("VX");
            boolean vitO = estadoFinal.equals("VO");
            boolean derrotaX = estadoFinal.equals("VO");
            boolean derrotaO = estadoFinal.equals("VX");

            System.out.println("🏁 Jogo terminado! Atualizando base de dados (XML)...");
            
            // Atualiza estatísticas do Jogador X
            User.registarResultadoJogo(jogadorX.user.getUsername(), vitX, derrotaX, tempoDecorridoSecs);
            // Atualiza estatísticas do Jogador O
            User.registarResultadoJogo(jogadorO.user.getUsername(), vitO, derrotaO, tempoDecorridoSecs);
            
            // -------------------------------------------

		} catch (Exception e) {
			System.out.println("Servidor dedicado: terminou o jogo ("+e.getMessage()+")!");
			// e.printStackTrace();
		} finally {
			// Garante que os sockets são fechados, mesmo em caso de exceção
			try {
				connectionX.close();
				connectionO.close();
			} catch (IOException e) {
				// Ignora a exceção caso ocorra algum erro ao fechar
			}
		}
		System.out.println("Servidor dedicado: terminou a Thread ("+ this.threadId()+") do servidor dedicado!");
	} // fim run
} // end Servidor Dedicado