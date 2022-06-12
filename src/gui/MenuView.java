package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import game.Game;
import game.Launcher;

import javax.swing.border.EtchedBorder;
import java.awt.Canvas;
import java.awt.Button;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.awt.event.ActionEvent;

public class MenuView extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MenuView frame = new MenuView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MenuView() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 236);
		setTitle("[¿€º∫¿⁄ : ¿Â¡æ±‘] Alpha Beta Pruning¿ª ¿ÃøÎ«— Othello AI ver 0.03");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Othello AI Hermes ver 0.03",JLabel.CENTER);
		lblNewLabel.setFont(new Font("±º∏≤", Font.BOLD, 20));
		lblNewLabel.setBounds(91, 10, 266, 53);
		contentPane.add(lblNewLabel);
		
		JButton btnNewButton = new JButton("AI vs White");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						Launcher.start1P();
						return null;
					}
					
				}.execute();
			}
		});
		btnNewButton.setFont(new Font("±º∏≤", Font.BOLD, 13));
		btnNewButton.setBounds(12, 73, 119, 95);
		contentPane.add(btnNewButton);
		
		JButton button = new JButton("AI vs Black");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						Launcher.start2P();
						return null;
					}
					
				}.execute();
			}
		});
		button.setFont(new Font("±º∏≤", Font.BOLD, 13));
		button.setBounds(159, 73, 119, 95);
		contentPane.add(button);
		
		JButton button_1 = new JButton("AI vs AI");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				new SwingWorker() {
					@Override
					protected Object doInBackground() throws Exception {
						Launcher.startAItoAI();
						return null;
					}
					
				}.execute();
			}
		});
		button_1.setFont(new Font("±º∏≤", Font.BOLD, 13));
		button_1.setBounds(303, 73, 119, 95);
		contentPane.add(button_1);
	}
}
