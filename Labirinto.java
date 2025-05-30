/**
 * Labirinto de Mistérios.
 * 
 * Classes:
 * - Posicao: Representa uma posição (x, y) no labirinto.
 * - Tesouro: Representa um tesouro no labirinto, com nome, posição e valor.
 * - Perigo: Representa um perigo no labirinto, com posição e dano.
 * - Aventureiro: Representa o jogador, com nome, posição, tesouros coletados e pontos.
 * - RankingEntry: Representa uma entrada no ranking de pontuação.
 * - Labirinto: Classe principal, interface gráfica e lógica do jogo.
 * 
 * Funcionalidades principais:
 * - Geração aleatória do labirinto com paredes, tesouros e perigos.
 * - Movimento do jogador usando as teclas direcionais.
 * - Coleta de tesouros e acúmulo de pontos.
 * - Detecção de perigos (game over).
 * - Progressão de níveis ao coletar todos os tesouros.
 * - Seleção de skins para o personagem.
 * - Exibição de ranking dos jogadores.
 * - Uso de poderes especiais para destruir paredes (limitado a 2 usos por jogo).
 * 
 * Interface gráfica:
 * - Menu inicial para inserir nome, escolher skin, iniciar jogo ou ver ranking.
 * - Desenho do labirinto, jogador, tesouros e perigos.
 * - Mensagens de game over e avanço de nível.
 * 
 * Observações:
 * - As imagens das skins devem estar nos caminhos especificados.
 * - O ranking é mantido apenas durante a execução do programa.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

class Posicao {
    public int x, y;
    public Posicao(int x, int y) { this.x = x; this.y = y; }
    @Override
    public boolean equals(Object o) {
        if (o instanceof Posicao p) {
            return x == p.x && y == p.y;
        }
        return false;
    }
}

class Tesouro {
    protected String nome;
    protected Posicao posicao;
    protected int valor;

    public Tesouro(String nome, Posicao posicao, int valor) {
        this.nome = nome;
        this.posicao = posicao;
        this.valor = valor;
    }

    public int getValor() { return valor; }
    public Posicao getPosicao() { return posicao; }
    public void efeito(Aventureiro a) {
        a.coletarTesouro(this);
    }
}

class Perigo {
    private Posicao posicao;
    private int dano;

    public Perigo(Posicao posicao, int dano) {
        this.posicao = posicao;
        this.dano = dano;
    }

    public Posicao getPosicao() { return posicao; }
}

class Aventureiro {
    private String nome;
    private Posicao posicao;
    private ArrayList<Tesouro> coletados;
    private int pontos;

    public Aventureiro(String nome, Posicao inicio) {
        this.nome = nome;
        this.posicao = inicio;
        this.coletados = new ArrayList<>();
        this.pontos = 0;
        this.usosDePoder = 2; 
    }
    

    public String getNome() { return nome; }
    public Posicao getPosicao() { return posicao; }
    public void setPosicao(Posicao p) { posicao = p; }
    public void coletarTesouro(Tesouro t) {
        coletados.add(t);
        pontos += t.getValor();
    }
    public int getPontos() { return pontos; }
    public int getUsosDePoder() {
    return usosDePoder;}

    private int usosDePoder = 2;

    public boolean usarPoder(char[][] labirinto) {
        if (usosDePoder <= 0) return false;

        int[][] direcoes = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        for (int[] dir : direcoes) {
            int nx = posicao.x + dir[0];
            int ny = posicao.y + dir[1];
            if (nx >= 0 && nx < labirinto.length && ny >= 0 && ny < labirinto[0].length) {
                if (labirinto[nx][ny] == '#') {
                    labirinto[nx][ny] = '.';  
                    usosDePoder--;
                    return true;
                }
            }
        }
        return false;
    }
 

}

class RankingEntry {
    String nome;
    int pontos;

    public RankingEntry(String nome, int pontos) {
        this.nome = nome;
        this.pontos = pontos;
    }
}

public class Labirinto extends JFrame {
    private final int TAM = 10;
    private final int TILE_SIZE = 50;
    private char[][] estrutura;
    private Aventureiro jogador;
    private ArrayList<Tesouro> tesouros;
    private ArrayList<Perigo> perigos;
    private int nivel = 1;
    private ArrayList<RankingEntry> ranking = new ArrayList<>();

    private JTextField nomeField;
    private JPanel menuPanel;
    private JButton jogarBtn, rankingBtn;
    private JComboBox<String> skinBox;
    private boolean emJogo = false;

    private String[] skins = {"kevin_hart_gato", "rudiger", "sociologo", "batman"};
    private String skinSelecionada = "kevin_hart_gato";

    private BufferedImage kevinSkin, rudigerSkin, sociologoSkin, batmanSkin;

    public Labirinto() throws IOException {
        setTitle("Labirinto de Mistérios - Menu");
        setSize(600, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        kevinSkin = redimensionarImagem("C:/Users/ferra/Downloads/LabirintoPOO/8046d7231f6d1b147cafa7ad53aaea42.jpg");
        rudigerSkin = redimensionarImagem("C:/Users/ferra/Downloads/LabirintoPOO/ijdulbl2k9xe1.jpeg");
        sociologoSkin = redimensionarImagem("C:/Users/ferra/Downloads/LabirintoPOO/FoEWd1-XwAANPEm.jpg");
        batmanSkin = redimensionarImagem("C:/Users/ferra/Downloads/LabirintoPOO/download.jpg");

        menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(5, 1, 10, 10));
        menuPanel.setBounds(150, 200, 300, 250);

        JLabel titulo = new JLabel("Labirinto de Mistérios", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));

        nomeField = new JTextField("Jogador");
        skinBox = new JComboBox<>(skins);

        jogarBtn = new JButton("Jogar");
        rankingBtn = new JButton("Ver Ranking");

        jogarBtn.addActionListener(e -> iniciarJogo());
        rankingBtn.addActionListener(e -> mostrarRanking());

        menuPanel.add(titulo);
        menuPanel.add(nomeField);
        menuPanel.add(skinBox);
        menuPanel.add(jogarBtn);
        menuPanel.add(rankingBtn);

        add(menuPanel);

        configurarTeclas();

        addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            int tecla = e.getKeyCode();

            if (!emJogo) return;

            switch (tecla) {
                case KeyEvent.VK_W -> moverJogador(-1, 0);
                case KeyEvent.VK_S -> moverJogador(1, 0);
                case KeyEvent.VK_A -> moverJogador(0, -1);
                case KeyEvent.VK_D -> moverJogador(0, 1);
                case KeyEvent.VK_P -> {
                    boolean usado = jogador.usarPoder(estrutura);
                    if (usado) {
                        JOptionPane.showMessageDialog(null, "Parede destruída!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Nenhuma parede destruída ou usos esgotados.");
                    }
                    repaint();
                }
            }
        }
    });

        setVisible(true);
    }

    private BufferedImage redimensionarImagem(String caminho) throws IOException {
        BufferedImage original = ImageIO.read(new File(caminho));
        Image scaled = original.getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();
        return resized;
    }

    public void configurarTeclas() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke("UP"), "moverCima");
        im.put(KeyStroke.getKeyStroke("DOWN"), "moverBaixo");
        im.put(KeyStroke.getKeyStroke("LEFT"), "moverEsquerda");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "moverDireita");
        im.put(KeyStroke.getKeyStroke("P"), "usarPoder");

        am.put("moverCima", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (emJogo) moverJogador(-1, 0); } });
        am.put("moverBaixo", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (emJogo) moverJogador(1, 0); } });
        am.put("moverEsquerda", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (emJogo) moverJogador(0, -1); } });
        am.put("moverDireita", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (emJogo) moverJogador(0, 1); } });
        am.put("usarPoder", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (emJogo) {
                    boolean usado = jogador.usarPoder(estrutura);
                    if (usado) {
                        JOptionPane.showMessageDialog(null, "Parede destruída!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Nenhuma parede destruída ou usos esgotados.");
                    }
                    repaint();
                }
            }
        });
    }

    public void iniciarJogo() {
        jogador = new Aventureiro(nomeField.getText(), new Posicao(0, 0));
        skinSelecionada = (String) skinBox.getSelectedItem();
        gerarLabirinto();
        emJogo = true;
        menuPanel.setVisible(false);
        setTitle("Labirinto de Mistérios - Jogando");
        repaint();
    }

    public void gerarLabirinto() {
        estrutura = new char[TAM][TAM];
        tesouros = new ArrayList<>();
        perigos = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                estrutura[i][j] = '.';
                if (rand.nextDouble() < 0.25) estrutura[i][j] = '#';
            }
        }

        estrutura[0][0] = 'P';

        for (int i = 0; i < 5; i++) {
            Posicao p;
            do { p = new Posicao(rand.nextInt(TAM), rand.nextInt(TAM)); } while (estrutura[p.x][p.y] != '.');
            tesouros.add(new Tesouro("Tesouro" + i, p, 100));
        }

        for (int i = 0; i < 3; i++) {
            Posicao p;
            do { p = new Posicao(rand.nextInt(TAM), rand.nextInt(TAM)); } while (estrutura[p.x][p.y] != '.');
            perigos.add(new Perigo(p, 50));
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!emJogo) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < TAM; i++) {
            for (int j = 0; j < TAM; j++) {
                Posicao pos = new Posicao(i, j);
                Color cor = estrutura[i][j] == '#' ? new Color(60, 60, 60) : new Color(230, 230, 230);

                for (Tesouro t : tesouros) if (t.getPosicao().equals(pos)) cor = new Color(255, 223, 0);
                for (Perigo p : perigos) if (p.getPosicao().equals(pos)) {
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(1));
    g2.drawRect(j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE);
    g2.setFont(new Font("Dialog", Font.PLAIN, 12));
    g2.drawString("!", j * TILE_SIZE + TILE_SIZE / 2 - 4, i * TILE_SIZE + 40 + TILE_SIZE / 2 + 4);
}

                g2.setColor(cor);
                g2.fillRoundRect(j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE, 12, 12);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRoundRect(j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE, 12, 12);

                if (jogador.getPosicao().equals(pos)) {
                    switch (skinSelecionada) {
                        case "kevin_hart_gato" -> g2.drawImage(kevinSkin, j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE, null);
                        case "rudiger" -> g2.drawImage(rudigerSkin, j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE, null);
                        case "sociologo" -> g2.drawImage(sociologoSkin, j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE, null);
                        case "batman" -> g2.drawImage(batmanSkin, j * TILE_SIZE, i * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE, null);
                        default -> {
                            g2.setColor(new Color(30, 180, 80));
                            g2.fillOval(j * TILE_SIZE + 10, i * TILE_SIZE + 50, TILE_SIZE - 20, TILE_SIZE - 20);
                        }
                    }
                }
            }
        }

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Verdana", Font.BOLD, 18));
        g2.drawString("Nível: " + nivel + "  Pontos: " + jogador.getPontos(), 10, 30);
    }

    public void moverJogador(int dx, int dy) {
        Posicao atual = jogador.getPosicao();
        int novoX = atual.x + dx;
        int novoY = atual.y + dy;

        if (novoX >= 0 && novoX < TAM && novoY >= 0 && novoY < TAM && estrutura[novoX][novoY] != '#') {
            Posicao nova = new Posicao(novoX, novoY);

            for (Perigo p : perigos) {
                if (p.getPosicao().equals(nova)) {
                    JOptionPane.showMessageDialog(this, "VOCÊ MORREU!", "Game Over", JOptionPane.ERROR_MESSAGE);
                    ranking.add(new RankingEntry(jogador.getNome(), jogador.getPontos()));
                    emJogo = false;
                    menuPanel.setVisible(true);
                    setTitle("Labirinto de Mistérios - Menu");
                    repaint();
                    return;
                }
            }

            tesouros.removeIf(t -> {
                if (t.getPosicao().equals(nova)) { t.efeito(jogador); return true; }
                return false;
            });

            jogador.setPosicao(nova);
            estrutura[atual.x][atual.y] = '.';
            estrutura[nova.x][nova.y] = 'P';

            if (tesouros.isEmpty()) {
                nivel++;
                JOptionPane.showMessageDialog(this, "Parabéns! Nível " + nivel + " alcançado!");
                gerarLabirinto();
                repaint();
            }
            repaint();
        }
    }

    public void mostrarRanking() {
        if (ranking.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum jogador ainda!", "Ranking", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ranking.sort((a, b) -> b.pontos - a.pontos);

        StringBuilder sb = new StringBuilder("\n--- Ranking ---\n");
        for (RankingEntry r : ranking) sb.append(r.nome).append(" - ").append(r.pontos).append(" pts\n");

        JOptionPane.showMessageDialog(this, sb.toString(), "Ranking", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) throws IOException {
        new Labirinto();
    }
}
