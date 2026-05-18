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
        // Adicionamos os atributos 'linhas' e 'colunas' na raiz para o Stub não falhar
        String tab = "<tabuleiro estado='" + estado + "' " +
                     "linhas='" + pontosLinhas + "' " +
                     "colunas='" + pontosColunas + "'>";

        // Linhas Horizontais
        for (int i = 0; i < pontosLinhas; i++) {
            for (int j = 0; j < pontosColunas - 1; j++) {
                // Usamos "H" e "S"/"N" para bater com a lógica do Stub
                String ocupada = linhasHorizontais[i][j] ? "S" : "N";
                tab += "<linha tipo='H' linha='" + i + "' coluna='" + j + "' ocupada='" + ocupada + "'/>";
            }
        }

        // Linhas Verticais
        for (int i = 0; i < pontosLinhas - 1; i++) {
            for (int j = 0; j < pontosColunas; j++) {
                String ocupada = linhasVerticais[i][j] ? "S" : "N";
                tab += "<linha tipo='V' linha='" + i + "' coluna='" + j + "' ocupada='" + ocupada + "'/>";
            }
        }

        // Caixas
        for (int i = 0; i < pontosLinhas - 1; i++) {
            for (int j = 0; j < pontosColunas - 1; j++) {
                // Adicionamos linha e coluna explicitamente na tag caixa
                tab += "<caixa linha='" + i + "' coluna='" + j + "' dono='" + caixas[i][j] + "'/>";
            }
        }

        tab += "</tabuleiro>";
        return tab;
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