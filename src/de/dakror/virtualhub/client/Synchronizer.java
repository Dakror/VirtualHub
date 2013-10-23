package de.dakror.virtualhub.client;

/**
 * @author Dakror
 */
public class Synchronizer extends Thread
{
	ClientFrame frame;
	
	public Synchronizer()
	{
		frame = Client.currentClient.frame;;
		
		// start();
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			// check sync if fileView
			if (frame.getSelectedTreeFile() != null)
			{
				// if (frame.fileView.getComponentCount() != Assistant.getLegitFileCount(frame.getSelectedTreeFile()) && frame.directoryLoader.synced) frame.directoryLoader.fireUpdate();
			}
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
