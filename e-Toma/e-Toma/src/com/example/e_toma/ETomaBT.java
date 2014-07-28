package com.example.e_toma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class ETomaBT {
	public static final String BT_STATUS_OK = "BT Status OK";

	public static final String BT_DESLIGADO = "BT Desligado.";

	private static final String TAG = "ETomaBT";

	@SuppressLint("HandlerLeak")
	private final Handler h = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case RECIEVE_MESSAGE: // if receive massage
				byte[] readBuf = (byte[]) msg.obj;
				String strIncom = new String(readBuf, 0, msg.arg1); // create
																	// string
																	// from
																	// bytes
																	// array
				sb.append(strIncom); // append string
				int endOfLineIndex = sb.indexOf("\r\n"); // determine the
															// end-of-line
				if (endOfLineIndex == 0)
					endOfLineIndex = sb.indexOf("\n");
				
				if (endOfLineIndex > 0) { // if end-of-line,
					String sbprint = sb.substring(0, endOfLineIndex); // extract
																		// string
					sb.delete(0, sb.length()); // and clear

					// para retirar o "|" do final da mensagem
					if (sbprint.contains("|")) {
						sbprint = sbprint.substring(0, endOfLineIndex - 1);
					}
					
					// tratar o caso em que recebe valor 0
					if (sbprint.equals("|")){
						sbprint = "0";
					}
					
					mETomaListener.lerMensagem(sbprint); // update TextView

				}
				Log.d(TAG, "...String:" + sb.toString() + "Byte:"
						+ msg.arg1 + "...");
				break;
			}
		};
	};

	final int RECIEVE_MESSAGE = 1; // Status for Handler
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private StringBuilder sb = new StringBuilder();

	private ConnectedThread mConnectedThread;

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// MAC-address of Bluetooth module (you must edit this line)
	private static String addressBafuino = "20:13:06:24:00:08"; // BAFUÍNO
	private static String addressTeste = "00:11:F6:0B:7F:03"; // TRT066807

	private Activity mActivity;
	
	private boolean btLigado = false;

	private ETomaBTListener mETomaListener;

	public interface ETomaBTListener {
		/**
		 * Called when new incoming data is available.
		 */
		public void lerMensagem(String msg);
	}

	public ETomaBT(Activity act, ETomaBTListener listener) {
		mActivity = act;
		mETomaListener = listener;

	}

	public String inicializar() {
		String ret = new String(""); 

		btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth
														  // adapter
		ret += checkBTState();
		
		if (!ret.equals(BT_DESLIGADO)){
			btLigado = true;
		} else {
			btLigado = false;
		}
		
		if (btLigado){

			Log.d(TAG, "...inicializar - tentando conectar...");
	
			if (!conectar(addressBafuino)){
				if (!conectar(addressTeste)){
					ret += "\nNão foi possível conectar ao dispositivo bluetooth.";
					btLigado = false;
				}
			}
	
			// Create a data stream so we can talk to server.
			Log.d(TAG, "...Create Socket...");
	
			mConnectedThread = new ConnectedThread(btSocket);
			mConnectedThread.start();
			
		}

		return ret;
	}

	public boolean conectar(String addr) {
		// Set up a pointer to the remote node using it's address.
		BluetoothDevice device = btAdapter.getRemoteDevice(addr);

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e) {
			return false;
		}

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection. This will block until it connects.
		Log.d(TAG, "...Connecting "+addr);
		try {
			btSocket.connect();
			Log.d(TAG, "....Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
				Log.d(TAG, "....Connection erro " + e.getMessage());
				return false;
			} catch (IOException e2) {
				Log.d(TAG, "....Connection erro " + e2.getMessage());
				return false;
			}
		}
		
		return true;
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	public String finalizar() {
		String ret = new String("");

		Log.d(TAG, "...finalizar...");

		try {
			btSocket.close();
			ret += "Comunicação bluetooth finalizada.";
		} catch (IOException e2) {
			ret += "Fatal Error: finalizar() and failed to close socket."
					+ e2.getMessage() + ".";
		}

		return ret;
	}

	private String checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned
		// on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter == null) {
			return new String("Fatal Error: Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");
				return new String(BT_STATUS_OK);
			} else {
				// Prompt user to turn on Bluetooth
				/*
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				mActivity.startActivityForResult(enableBtIntent, 1);
				*/
				return new String(BT_DESLIGADO);
			}
		}
	}

	public String enviarMsg(String msg) {
		
		if (!btLigado){
			return "Erro: bluetooth está desligado. Tente reconectar.";
		}

		if (msg.trim().length() == 0){
			return "Erro: não é possível enviar mensagem vazia.";
		}
		
		msg = msg.trim();

		String ret = new String("");

		try {
			mConnectedThread.write(msg);

			ret += "Mensagem enviada: " + msg + " ("+msg.length()+" bytes).";
		} catch (Exception e) {
			ret += "Erro ao enviar mensagem: "+e.getMessage();
		}

		return ret;
	}

	private class ConnectedThread extends Thread {
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[256]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					if (btLigado){
						// Read from the InputStream
						bytes = mmInStream.read(buffer); // Get number of bytes and
															// message in "buffer"
						h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer)
								.sendToTarget(); // Send to message queue Handler
					}
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(String message) {
			Log.d(TAG, "...Data to send: " + message + "...");
			byte[] msgBuffer = message.getBytes();
			try {
				mmOutStream.write(msgBuffer);
			} catch (IOException e) {
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
			} catch (Exception e){
				Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
			}
		}
	}

}
