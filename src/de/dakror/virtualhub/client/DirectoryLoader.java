/*******************************************************************************
 * Copyright 2015 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.dakror.virtualhub.client;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.dakror.virtualhub.client.file.EticetableTreeNode;
import de.dakror.virtualhub.client.file.FileButton;
import de.dakror.virtualhub.client.tags.TagsTreeCellRender;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.FileComparator;

/**
 * @author Dakror
 */
public class DirectoryLoader extends Thread {
	static DirectoryLoader loader;
	
	private ClientFrame frame;
	
	EticetableTreeNode selectedNode;
	
	boolean synced;
	
	public DirectoryLoader() {
		frame = Client.currentClient.frame;
		setName("DirectoryLoader");
		
		setPriority(MAX_PRIORITY);
		
		synced = true;
		
		start();
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1);
				
				if (frame.catalog.getSelectionPath() == null) {
					if (selectedNode == null) {
						continue;
					}
					frame.fileView.dropTarget.setActive(false);
					frame.fileView.removeAll();
					frame.fileView.validate();
					frame.repaint();
					selectedNode = null;
					continue;
				}
				
				final EticetableTreeNode dmtn = (EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
				if (selectedNode == null || !dmtn.equals(selectedNode)) {
					selectedNode = dmtn;
					synced = false;
				} else continue;
				
				frame.fileView.dropTarget.setActive(true);
				frame.fileViewWrap.getVerticalScrollBar().setValue(0);
				frame.fileView.removeAll();
				
				File folder = new File(Assistant.getNodePath(dmtn));
				if (!folder.exists()) continue;
				
				List<File> files = Arrays.asList(folder.listFiles());
				Collections.sort(files, new FileComparator());
				
				for (File f : files) {
					if (f.isHidden()) continue;
					
					if (frame.catalog.getSelectionPath() == null || !((EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent()).equals(dmtn)) break;
					
					final FileButton fb = new FileButton(f);
					
					final JPopupMenu popup = new JPopupMenu();
					popup.add(new JMenuItem(new AbstractAction("Löschen") {
						private static final long serialVersionUID = 1L;
						
						@Override
						public void actionPerformed(ActionEvent e) {
							if (frame.getSelectedFiles().length < 2) {
								if (JOptionPane.showConfirmDialog(frame, "Sind Sie sicher, dass Sie diese" + (fb.file.isDirectory() ? "s Verzeichnis und alle enthaltenen Dateien\n" : " Datei")
										+ " unwiderruflich löschen wollen?\nSie sollten von wichtigen Daten Backups machen, bevor Sie sie löschen.", (fb.file.isDirectory() ? "Verzeichnis" : "Datei")
										+ " löschen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
									boolean success = true;
									boolean directory = fb.file.isDirectory();
									
									if (directory) {
										success = Assistant.deleteDirectory(fb.file);
										
										EticetableTreeNode dmtn = (EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
										frame.loadSubTree(dmtn);
										
									} else success = fb.file.delete();
									
									if (!success) JOptionPane.showMessageDialog(frame, (directory ? "Das Verzeichnis" : "Die Datei")
											+ " konnte nicht gelöscht werden, da sie in einem anderen Programm geöffnet ist.", "Löschen nicht möglich", JOptionPane.ERROR_MESSAGE);
									else fireUpdate();
								}
							} else if (JOptionPane.showConfirmDialog(	frame,
																												"Sind Sie sicher, dass Sie die Daten unwiderruflich löschen wollen?\nSie sollten von wichtigen Daten Backups machen, bevor Sie sie löschen.",
																												"Daten löschen", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
								File[] files = frame.getSelectedFiles();
								for (File file : files) {
									boolean success = true;
									boolean directory = file.isDirectory();
									
									if (directory) {
										success = Assistant.deleteDirectory(file);
										
										if (success) {
											EticetableTreeNode dmtn = (EticetableTreeNode) frame.catalog.getSelectionPath().getLastPathComponent();
											for (int i = 0; i < dmtn.getChildCount(); i++)
												if (((EticetableTreeNode) dmtn.getChildAt(i)).getUserObject().toString().equals(fb.file.getName())) dmtn.remove(i);
											
											((DefaultTreeModel) frame.catalog.getModel()).reload(dmtn);
										}
									} else success = file.delete();
									
									if (!success)
										JOptionPane.showMessageDialog(frame, (directory ? "Das Verzeichnis" : "Die Datei")
												+ " konnte nicht gelöscht werden, da sie in einem anderen Programm geöffnet ist.", "Löschen nicht möglich", JOptionPane.ERROR_MESSAGE);
								}
								fireUpdate();
							}
						}
					}));
					
					fb.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (e.getModifiers() == 16) // nothing except click
							{
								for (Component c : frame.fileView.getComponents()) {
									if (c instanceof FileButton) {
										if (!c.equals(fb)) ((FileButton) c).setSelected(false);
									}
								}
								fb.setSelected(fb.isSelected());
							} else if (e.getModifiers() == 18) // ctrl
							{
								fb.setSelected(fb.isSelected());
							} else if (e.getModifiers() == 17) // shift
							{
								if (frame.getSelectedFiles().length == 2 && fb.isSelected()) {
									for (int i = frame.getFileIndex(frame.getSelectedFiles()[0]); i < frame.fileView.getComponentCount(); i++) {
										((FileButton) frame.fileView.getComponent(i)).setSelected(true);
										if (((FileButton) frame.fileView.getComponent(i)).file.equals(fb.file)) break;
									}
								}
							}
							
							
							frame.setFileInfo(fb);
						}
					});
					fb.addFocusListener(new FocusListener() {
						
						@Override
						public void focusLost(FocusEvent e) {
							((TagsTreeCellRender) frame.tags.getCellRenderer()).selectedFile = null;
							frame.tags.repaint();
						}
						
						@Override
						public void focusGained(FocusEvent e) {
							((TagsTreeCellRender) frame.tags.getCellRenderer()).selectedFile = fb;
							frame.tags.repaint();
						}
					});
					fb.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
								if (fb.file.isDirectory()) {
									frame.catalog.expandPath(new TreePath(dmtn.getPath()));
									for (int i = 0; i < dmtn.getChildCount(); i++) {
										if (((EticetableTreeNode) dmtn.getChildAt(i)).getUserObject().equals(fb.file.getName())) {
											frame.catalog.setSelectionPath(new TreePath(((EticetableTreeNode) dmtn.getChildAt(i)).getPath()));
										}
									}
								} else {
									if (Desktop.isDesktopSupported()) {
										try {
											Desktop.getDesktop().browse(fb.file.toURI());
										} catch (IOException e1) {
											JOptionPane.showMessageDialog(frame, "Die Datei konnte nicht geöffnet werden!\nMöglicherweise fehlt Ihnen eine mit diesem Dateityp assozierte Software.",
																										"Datei konnte nicht geöffnet werden!", JOptionPane.ERROR_MESSAGE);
										}
									}
								}
							} else if (e.getButton() == MouseEvent.BUTTON3) popup.show(e.getComponent(), e.getX(), e.getY());
						}
					});
					fb.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							if (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) {
								for (Component c : frame.fileView.getComponents()) {
									((FileButton) c).setSelected(!((FileButton) c).isSelected());
								}
							}
						}
					});
					frame.fileView.add(fb);
					
					frame.fileView.revalidate();
					frame.repaint();
					
					fb.requestFileData();
				}
				
				frame.fileView.revalidate();
				frame.repaint();
				
				System.gc();
				
				synced = true;
			} catch (InterruptedException e) {} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void fireUpdate() {
		frame.fileView.removeAll();
		frame.fileView.validate();
		frame.fileView.repaint();
		
		synced = false;
		selectedNode = null;
	}
}
