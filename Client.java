import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class Client {

    String host;
    private int port;  
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("Simple Chat");
    JTextArea messageArea = new JTextArea(40, 80);
    JTextField textField = new JTextField(60);

   
    public Client(String host, int port) {
        this.host = host;
        this.port = port;

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
     
    }

     private String getUserName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose your username:",
            "Username selection",
            JOptionPane.PLAIN_MESSAGE
        );
    }
     
    private void run() throws IOException {
        try {
            Socket socket = new Socket(host, port);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) 
            {
               String line = in.nextLine();
                if (line.startsWith("WHO")) {
                    out.println(getUserName());
                } 
                else if (line.startsWith("ACCEPTED")) {
                    this.frame.setTitle("User - " + line.substring(8));
                    textField.setEditable(true);
                } 
                else if (line.startsWith("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(frame, "Other player left");
                    break;
                }
                else if(line.startsWith("FINISH"))
                {
                    JOptionPane.showMessageDialog(frame, line.substring(6));
                    break;
                }
                else  {
                     messageArea.append(line + "\n");
                }
             
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int portNumber = 8888;
     
    if (args.length < 2) 
    {
      System.out.println("Default host = " + host + " \n Default port port = " + portNumber);
      Client client = new Client(host, portNumber);
      
      client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      client.frame.setVisible(true);
      client.run();
    } 
    else 
    {
       host = args[0];
       portNumber = Integer.valueOf(args[1]);
       Client client = new Client(host, portNumber);
       
       client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       client.frame.setVisible(true);
       client.run();
    }
        
    }
}