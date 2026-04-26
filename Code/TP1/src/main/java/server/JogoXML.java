package server;

/**
 * Classe que implementa o adaptador do jogo para XML.
 *
 * @author Engº Porfírio Filipe
 */
public class JogoXML extends Jogo {

    /**
     * Estado do jogo após a última jogada.
     * Possíveis valores:
     * - "ND": Nada a registar, continua o jogo.
     * - "IV": Jogada inválida.
     * - "VX": Vitória do X.
     * - "VO": Vitória do O.
     * - "EM": Empate.
     * - "BN": Jogada Bonus.
     */
    private String estado = "ND";

    /**
     * Converte o tabuleiro do jogo para XML e inclui o estado.
     *
     * @return String com XML que representa o tabuleiro.
     */
    public String tabuleiroToXML() {
        String tab = "<tabuleiro estado='" + estado + "' >";
        for (int i = 0; i < pontosLinhas; i++) {
            for (int j = 0; j < pontosColunas - 1; j++) {
                tab += "<linha tipo='Horizontal' linha='" + i + "' coluna='" + j + "' ocupada='" + linhasHorizontais[i][j] + "'/>";
            }
        }
        for (int i = 0; i < pontosLinhas - 1; i++) {
            for (int j = 0; j < pontosColunas; j++) {
                tab += "<linha tipo='Vertical' linha='" + i + "' coluna='" + j + "' ocupada='" + linhasVerticais[i][j] + "'/>";
            }
        }
        for (int i = 0; i < pontosLinhas - 1; i++) {
            for (int j = 0; j < pontosColunas - 1; j++) {
                tab += "<caixa dono='" + caixas[i][j] + "'/>";
            }
        }

        return tab += "</tabuleiro>";
    }

    /**
     * Concretiza a jogada e atualiza o estado do jogo.
     *
     */
	public boolean joga(short numero, char simbolo) {
		estado = "ND"; // Nada a registar, continua o jogo.

		// Verifica se a jogada é válida no jogo base.
		if (!super.joga(numero, simbolo)) {
			estado = "IV"; // Jogada inválida.
			return false;
		}
        if(super.ultimaJogadaFechouCaixa()){
            if(super.terminou()){
                definirVencedor();
            } else{
                estado = "BN";
            }
        } else{
            if(super.terminou()){
                definirVencedor();
            }
        }

		return true;
	}

    private void definirVencedor() {
        if (super.empate()) {
            estado = "EM";
        } else if (super.vitoria('X')) {
            estado = "VX";
        } else if (super.vitoria('O')) {
            estado = "VO";
        }
    }

    /**
     * Indica se o jogo terminou com base no estado atual.
     *
     * @return true se o jogo terminou, false caso contrário.
     */
    public boolean terminou() {
        return !estado.equals("ND") && !estado.equals("IV");
    }

    public String getEstado(){
        return estado;
    }
}