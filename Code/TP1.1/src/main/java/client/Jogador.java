package client;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.w3c.dom.Element;

import util.XMLDoc;

/**
 * Classe Jogador representa um jogador que se liga ao servidor
 * e joga o jogo do galo.
 *
 * @author Engº Porfírio Filipe
 */
public class Jogador {
	/**
     * Host por omissão do servidor (endereço IP).
     */
    private final static String DEFAULT_HOST = "localhost";

    /**
     * Porto por omissão do servidor.
     */
    private final static int DEFAULT_PORT = 5025;

    static String host = DEFAULT_HOST;
    static int port = DEFAULT_PORT;
    
    // Acesso único ao teclado, define um Scanner para ser reutilziado
    private static Scanner leitor = null; 
    
    // Configuração inicial do client
    public Jogador(String Host, int Port, Scanner Sc) {
    	host = Host;
    	port = Port;
    	leitor = Sc;
    }
    
    /**
     * Lê um número curto da entrada do utilizador.
     *
     * @param 	leitor Scanner para ler a entrada do utilizador.
     * @return 	Número curto válido (entre 1 e 9).
     */
    private static short readShort(Scanner leitor) {
        short numero = 0; // Variável para armazenar o número lido.
        // Loop para garantir que um número válido é lido.
        while (true) {
            // Verifica se o próximo token na entrada é um número curto.
            if (leitor.hasNextShort()) {
                // Lê o número curto.
                numero = leitor.nextShort();
                // Verifica se o número está entre 1 e 9.
                if (numero < 0 || numero > 12) {
                    // Mostra uma mensagem de erro.
                    System.out.println("Jogada inválida!");
                } else {
                    // Retorna o número lido.
                    return numero;
                }
            } else {
                // Ignora a linha atual da entrada.
                leitor.nextLine();
            }
        }
    }
    
    // Lê a senha em modo camuflado se estiver disponivel
	  private static String leSenha(String prompt, Scanner s) {
	      String senha=null;
	      if(System.console() != null)
		  senha = new String(System.console().readPassword(prompt, 5000));
	      else {
		  System.out.println(prompt);
		  senha = s.nextLine();
	      }
	      return senha;
	  }
                                  
    /**
     * Método principal do programa ClienteTCP.
     *
     * @param args argumentos da linha de comando: host e porta
     */
	  public static void main(String[] args) {
        // Lê os argumentos da linha de comando (se existirem).
        if (args != null && args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }  
        if(leitor == null) 
            leitor = new Scanner(System.in);
        
        try (   
            // Tenta criar um socket para se conectar ao servidor.
            Socket socket = new Socket(host, port);
            // Cria um adaptador para comunicar com o servidor. (Usa letra minúscula para a variável!)
            Stub stub = new Stub(socket)) {

            // Mostra informações sobre a ligação.
            System.out.println("Cliente -> Ligação estabelecida: " + socket);
            
            System.out.println("1 - Login | 2 - Criar Conta");
            int opcao = leitor.nextInt();
            leitor.nextLine(); // Limpa o 'Enter' que ficou pendurado do nextInt()!

            char simbolo = ' '; // Variável para guardar o 'X' ou 'O'

            if (opcao == 2) {
                // --- FLUXO DE REGISTO ---
                System.out.println("<<< ***** CRIAR NOVA CONTA ***** >>>");
                
                System.out.print("Indique o seu nickname: ");
                String nick = leitor.nextLine();
                
                String pass = leSenha("Indique a sua senha: ", leitor);
                
                System.out.print("Nacionalidade (Ex: PT, FR, UK): ");
                String nac = leitor.nextLine();
                
                System.out.print("Idade: ");
                int idade = leitor.nextInt();
                leitor.nextLine(); // Limpar o 'Enter'

                // NOVA LÓGICA PARA A FOTOGRAFIA:
                System.out.print("Indique o caminho COMPLETO para a sua fotografia");
                System.out.print("(Ex: C:\\Users\\O_Teu_Nome\\Desktop\\foto.jpg ou /Users/Nome/foto.jpg): ");
                String caminhoFoto = leitor.nextLine();

                // Usamos a classe do professor para ler a imagem e gerar a Base64
                util.MyImage img = new util.MyImage(caminhoFoto);
                if (!img.isOk()) {
                    System.out.println("Erro: Não foi possível ler a imagem! A abortar...");
                    return;
                }
                String foto = img.getBase64(); // A string gigante é gerada aqui de forma segura!

                // Agora sim, chamamos o registo
                simbolo = stub.registar(nick, pass, foto, nac, idade);
                System.out.println("Conta criada com sucesso! A entrar na fila de espera...");

            } else if (opcao == 1) {
                // --- FLUXO DE LOGIN ---
                System.out.println("<<< ***** LOGIN ***** >>>");
                System.out.println("Utilizadores existentes para teste:");
                System.out.println("cartwheel:p1; milkshake:p2; gandalf:p4; opera:p5; smoke:p9; bagel:p10");
                
                System.out.print("Indique o seu nome de utilizador: ");
                String nome = leitor.nextLine();
                
                String senha = leSenha("Indique a sua senha: ", leitor);
               
                // Inicia a sessão com o servidor e obtém o símbolo do jogador.
                simbolo = stub.iniciar(nome, senha);
            } else {
                System.out.println("Opção inválida! A fechar o jogo...");
                return;
            }
            
            // Daqui para a frente, o código é comum! Tanto o Registo como o Login
            // devolveram um símbolo e puseram o jogador no jogo.

            //stub.print(); // Opcional: imprimir dados do jogador
            System.out.println("Foi-lhe atribuído o identificador de jogador: " + simbolo);
            
            if(simbolo == '2') 
            {
                System.out.println("À espera que o oponente jogue...");
            }
            
            // Loop do jogo, enquanto não for o fim do jogo (estado != "ND")
            for(;;) {
                // Mostra o tabuleiro atual.
                Element tab = stub.obter();
                System.out.println(stub.tabuleiroPontosCaixasToTXT(tab));
                
                String estado = tab.getAttribute("estado");
                if(!estado.equals("ND")) 
                {
                    // Mostra o estado do jogo após a jogada.
                    System.out.println(stub.estadoToTXT(estado));
                    // O loop só deve quebrar se o jogo terminar (VX, VO, EM)
                    if(!estado.equals("IV") && !estado.equals("BN"))
                    {
                        break;
                    }
                }
                LocalDateTime inicio = LocalDateTime.now();
                
                // Pede ao jogador para fazer uma jogada.
                System.out.print("Joga " + simbolo + ": ");

                // Lê a jogada do jogador.
                short jogada = readShort(leitor);
                // System.out.println(XMLDoc.tempoDif(inicio)); // (Opcional, mede tempo)
                
                // Envia jogada para o servidor.
                stub.jogar(jogada);
            }
        } catch (Exception e) {
            System.err.println("Jogador: " + e.getLocalizedMessage());
            // e.printStackTrace();
        } finally {
            System.out.println("Jogador: terminou a execução!");
        }
    }
}