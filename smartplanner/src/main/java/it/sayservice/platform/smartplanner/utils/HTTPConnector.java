/**
 * Copyright 2011-2016 SAYservice s.r.l.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.sayservice.platform.smartplanner.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

public class HTTPConnector {

	public static void main(String args[]) {
		try {
			String res = doGet("http://localhost:7070/otp/routers/trento/transit/routes", "", null,
					MediaType.APPLICATION_JSON);
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String doGet(String address, String req, String accept, String contentType) throws Exception {

		StringBuffer response = new StringBuffer();

		URL url = new URL(address + "?" + req);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		// conn.setRequestProperty("Accept-Charset", "UTF-8");
		if (accept != null) {
			conn.setRequestProperty("Accept", accept);
		}
		if (contentType != null) {
			conn.setRequestProperty("Content-Type", contentType);
		}
		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader((conn.getInputStream()), "ISO-8859-1"));
		BufferedReader br = new BufferedReader(
				new InputStreamReader((conn.getInputStream()), Charset.defaultCharset()));

		String output = null;
		while ((output = br.readLine()) != null) {
			response.append(output);
		}

		conn.disconnect();

		String res = new String(response.toString().getBytes(), Charset.forName("UTF-8"));
		return res;
	}

	public static String doPost(String address, String req, String accept, String contentType) throws Exception {

		StringBuffer response = new StringBuffer();

		URL url = new URL(address + "?" + req);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		if (accept != null) {
			conn.setRequestProperty("Accept", accept);
		}
		if (contentType != null) {
			conn.setRequestProperty("Content-Type", contentType);
		}

		OutputStream out = conn.getOutputStream();
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		writer.write(req);
		writer.close();
		out.close();

		if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		String output = null;
		while ((output = br.readLine()) != null) {
			response.append(output);
		}

		conn.disconnect();

		return response.toString();
	}

}
