import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class BlackJack {

    //Inner class representing a single card in the deck
    private class Card {
        private final String value;
        private final String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        //numerical value of card
        public int getValue() {
            if ("JQK".contains(value)) return 10;
            if ("A".equals(value)) return 11;
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return "A".equals(value);
        }

        public String getImagePath() {
            return "./cards/" + this + ".png";
        }

        @Override
        public String toString() {
            return value + "-" + type;
        }
    }

    //Inner class for player or dealer hand
    private class Hand {
        private final List<Card> cards = new ArrayList<>();
        private int sum;
        private int aceCount;

        public void addCard(Card card) {
            cards.add(card);
            sum += card.getValue();
            if (card.isAce()) aceCount++;
        }

        public int getAdjustedSum() {
            int adjustedSum = sum;
            int currentAceCount = aceCount;
            
            while (adjustedSum > 21 && currentAceCount > 0) {
                adjustedSum -= 10;
                currentAceCount--;
            }
            return adjustedSum;
        }

        public void reset() {
            cards.clear();
            sum = 0;
            aceCount = 0;
        }

        public List<Card> getCards() {
            return cards;
        }
    }

    //UI and game constants
    private final int BOARD_WIDTH = 800;
    private final int BOARD_HEIGHT = 600;
    private final int CARD_WIDTH = 110;
    private final int CARD_HEIGHT = 154;

    private List<Card> deck;
    private Hand dealerHand;
    private Hand playerHand;
    private Card hiddenCard;

    private JFrame frame = new JFrame("♠️ Blackjack ♣️");
    private JPanel gamePanel = new GamePanel();
    private JButton hitButton = new ServiceButton("Hit", new Color(70, 130, 180));
    private JButton stayButton = new ServiceButton("Stay", new Color(220, 20, 60));
    private JButton resetButton = new ServiceButton("New Game", new Color(50, 205, 50));
    
    public BlackJack() {
        initializeUI();
        startNewGame();
    }

    private void initializeUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        gamePanel.setLayout(null);
        gamePanel.setBackground(new Color(0, 102, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(0, 76, 0));
        buttonPanel.add(hitButton);
        buttonPanel.add(stayButton);
        buttonPanel.add(resetButton);

        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(e -> handleHit());
        stayButton.addActionListener(e -> handleStay());
        resetButton.addActionListener(e -> handleReset());
        
        frame.setVisible(true);
    }

    private void startNewGame() {
        initializeDeck();
        initializeHands();
        dealInitialCards();
        hitButton.setEnabled(true);
        stayButton.setEnabled(true);
        gamePanel.repaint();
    }

    private void handleReset() {
        startNewGame();
    }

    private void initializeDeck() {
        deck = new ArrayList<>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (String type : types) {
            for (String value : values) {
                deck.add(new Card(value, type));
            }
        }
        Collections.shuffle(deck);
    }

    private void initializeHands() {
        dealerHand = new Hand();
        playerHand = new Hand();
        hiddenCard = null;
    }

    private void dealInitialCards() {
        // Dealer's hidden card
        hiddenCard = deck.remove(deck.size() - 1);
        dealerHand.addCard(hiddenCard);
        
        // Dealer's visible card
        dealerHand.addCard(deck.remove(deck.size() - 1));
        
        // Player's two cards
        playerHand.addCard(deck.remove(deck.size() - 1));
        playerHand.addCard(deck.remove(deck.size() - 1));
    }

    private void handleHit() {
        playerHand.addCard(deck.remove(deck.size() - 1));
        if (playerHand.getAdjustedSum() > 21) {
            hitButton.setEnabled(false);
        }
        gamePanel.repaint();
    }

    private void handleStay() {
        hitButton.setEnabled(false);
        stayButton.setEnabled(false);

        // Dealer draws until sum >= 17
        while (dealerHand.getAdjustedSum() < 17) {
            dealerHand.addCard(deck.remove(deck.size() - 1));
        }
        gamePanel.repaint();
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Draw gradient background
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 102, 0), 
                getWidth(), getHeight(), new Color(0, 76, 0));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            try {
                drawDealerHand(g2d);
                drawPlayerHand(g2d);
                drawGameResult(g2d);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void drawDealerHand(Graphics2D g) {
            // Draw hidden card
            ImageIcon backIcon = new ImageIcon(getClass().getResource("./cards/BACK.png"));
            Image hiddenImage = backIcon.getImage();
            if (!stayButton.isEnabled()) {
                hiddenImage = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
            }
            g.drawImage(hiddenImage, 50, 50, CARD_WIDTH, CARD_HEIGHT, null);

            // Draw dealer's visible cards
            List<Card> dealerCards = dealerHand.getCards();
            for (int i = 0; i < dealerCards.size(); i++) {
                Card card = dealerCards.get(i);
                Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                int x = 50 + (i * (CARD_WIDTH + 20));
                if (i == 0 && stayButton.isEnabled()) continue; // Skip hidden card if still playing
                g.drawImage(cardImage, x, 50, CARD_WIDTH, CARD_HEIGHT, null);
            }
        }

        private void drawPlayerHand(Graphics2D g) {
            List<Card> playerCards = playerHand.getCards();
            for (int i = 0; i < playerCards.size(); i++) {
                Card card = playerCards.get(i);
                Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                int x = 50 + (i * (CARD_WIDTH + 20));
                g.drawImage(cardImage, x, 350, CARD_WIDTH, CARD_HEIGHT, null);
            }
        }

        private void drawGameResult(Graphics2D g) {
            if (!stayButton.isEnabled()) {
                String message = determineGameOutcome();
                drawResultMessage(g, message);
            }
        }

        private String determineGameOutcome() {
            int playerSum = playerHand.getAdjustedSum();
            int dealerSum = dealerHand.getAdjustedSum();

            if (playerSum > 21) return "Bust! You Lose!";
            if (dealerSum > 21) return "Dealer Busts! You Win!";
            if (playerSum == dealerSum) return "Push! It's a Tie!";
            return playerSum > dealerSum ? "You Win!" : "You Lose!";
        }

        private void drawResultMessage(Graphics2D g, String message) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            
            // Calculate position
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(message)) / 2;
            int y = 250;

            // Draw background
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(x - 20, y - 40, fm.stringWidth(message) + 40, 60, 25, 25);
            
            // Draw text
            g.setColor(Color.YELLOW);
            g.drawString(message, x, y);
        }
    }

    private static class ServiceButton extends JButton {
        ServiceButton(String text, Color bgColor) {
            super(text);
            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setBackground(bgColor);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(brighter(bgColor));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(bgColor);
                }
            });
        }

        private static Color brighter(Color color) {
            int r = Math.min(255, color.getRed() + 30);
            int g = Math.min(255, color.getGreen() + 30);
            int b = Math.min(255, color.getBlue() + 30);
            return new Color(r, g, b);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BlackJack::new);
    }
}
