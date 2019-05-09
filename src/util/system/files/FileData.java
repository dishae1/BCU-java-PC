package util.system.files;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.imageio.ImageIO;

public interface FileData {

	public abstract BufferedImage getImg();

	public abstract Queue<String> readLine();

}

interface ByteData extends FileData {

	public byte[] getBytes();

	@Override
	public default BufferedImage getImg() {
		try {
			return ImageIO.read(new ByteArrayInputStream(getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public default Queue<String> readLine() {
		try {
			Queue<String> ans = new ArrayDeque<>();
			BufferedReader reader = null;
			InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(getBytes()), "UTF-8");
			reader = new BufferedReader(isr);
			String temp = null;
			while ((temp = reader.readLine()) != null)
				ans.add(temp);
			reader.close();
			return ans;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}