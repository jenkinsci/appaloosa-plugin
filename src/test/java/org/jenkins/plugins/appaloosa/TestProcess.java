package org.jenkins.plugins.appaloosa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.junit.Test;

public class TestProcess {

	@Test
	public void test() throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder("aapt", "dump", "badging", "/tmp/Google IO.apk");
		Process p = pb.start();
		p.waitFor();
		InputStream in = p.getInputStream();
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		StringWriter writer = new StringWriter();
		
		String line;
		line = r.readLine();
		while(null != line){
			writer.write(line);
			writer.write("\n");
			line = r.readLine();
		}
		
		String output = writer.getBuffer().toString();
		System.out.println(output);
	}

}
