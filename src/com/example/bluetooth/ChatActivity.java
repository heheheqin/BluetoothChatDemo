package com.example.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.entity.Bluetooth;
import com.example.utli.CloseIO;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ChatActivity extends Activity {
	private static final int STATUS_CONNECT_SUCCESS = 11;
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	private static final int SET_TITLE = 0;
	private boolean isOpen;
	private boolean servicflag;
	private Button disconnect;
	private ListView chat_list;
	private List<Bluetooth> arrayList = new ArrayList<>();
	private Bluetooth chatContent;
	private String bluetoothAddr;
	private String bluetoothName;
	private Button send_butt;
	private EditText edit;
	private MyBluetoothListAdapter chatAdapter;
	private BluetoothAdapter bluetoothAdapter;
	private BluetoothDevice mDevice;

	private ClientThread mClientThread;
	private ReadThread mReadThread;
	private ServerThread mServerThread;
	// 蓝牙服务端socket
	private BluetoothServerSocket mServerSocket;
	// 蓝牙客户端socket
	private BluetoothSocket mSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		initView();
		send_butt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String sendContent = edit.getText().toString().trim();
				if (!TextUtils.isEmpty(sendContent)) {
					sendMessagerHandler(sendContent, true);
					edit.setText("");

				} else {
					sendMessagerHandler("Love", false);
					Toast.makeText(ChatActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
				}
			}

		});

		disconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (servicflag) {
					shutdownServer();
				} else {
					shutdownClient();
				}
			}
		});

	}

	private void sendMessagerHandler(String sendContent, boolean flag) {
		if (mSocket == null) {
			Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			OutputStream os = mSocket.getOutputStream();
			os.write(sendContent.getBytes());
			addContnetList(sendContent, flag);

		} catch (IOException e) {
			e.printStackTrace();
			CloseIO.close(mSocket);
		}

	}

	public void addContnetList(String sendContent, boolean flag) {
		if (flag) {
			chatContent = new Bluetooth(R.drawable.wearable, "me", sendContent);
		} else {
			chatContent = new Bluetooth(R.drawable.game, bluetoothName, sendContent);
		}
		arrayList.add(chatContent);
		chatAdapter.notifyDataSetChanged();
		chat_list.setSelection(arrayList.size() - 1);
	}

	private void initView() {
		chat_list = (ListView) findViewById(R.id.chat_listview);
		send_butt = (Button) findViewById(R.id.send_but);
		edit = (EditText) findViewById(R.id.edit);
		disconnect = (Button) findViewById(R.id.disconnect);
		chatAdapter = new MyBluetoothListAdapter(arrayList, this);
		chat_list.setAdapter(chatAdapter);
		chat_list.setFastScrollEnabled(true);
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("bundle");
		servicflag = bundle.getBoolean("servicflag");
		if (servicflag) {
			setTitle("等待接入..");
		} else {
			bluetoothAddr = bundle.getString("bluetoothAddr");
			bluetoothName = bundle.getString("bluetoothName");
			setTitle(bluetoothName + "聊天中..");
			Toast.makeText(this, bluetoothName + ":" + bluetoothAddr, Toast.LENGTH_SHORT).show();
		}

		chatContent = new Bluetooth(R.drawable.game, bluetoothName, bluetoothAddr);
		arrayList.add(chatContent);
		chatAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isOpen) {
			Toast.makeText(this, "连接已经打开，可以通信。如果要再建立连接，请先断开", 
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (servicflag) {
			mServerThread = new ServerThread();
			mServerThread.start();
			isOpen = true;
		} else {
			if (!bluetoothAddr.equals("")) {
				mDevice = bluetoothAdapter.getRemoteDevice(bluetoothAddr);
				mClientThread = new ClientThread();
				mClientThread.start();
				isOpen = true;
			} else {
				Toast.makeText(this, "address is null !", Toast.LENGTH_SHORT).show();
			}
		}

	}

	// 开启服务器
	private class ServerThread extends Thread {
		

		public void run() {
			try {
				// 创建一个蓝牙服务器 参数分别：服务器名称、UUID
				mServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
						UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));

				Message msg = new Message();
				msg.obj = "请稍候，正在等待客户端的连接...";
				msg.what = STATUS_CONNECT_SUCCESS;
				mHandler.sendMessage(msg);

				/* 接受客户端的连接请求 */
				mSocket = mServerSocket.accept();
				bluetoothName = mSocket.getRemoteDevice().getName();
//				线程操作UI  挂掉
//				setTitle("与" + bluetoothName + "愉快的聊天..");
				msg = new Message();
				msg.obj = bluetoothName;
				msg.what = SET_TITLE;
				mHandler.sendMessage(msg);
				msg = new Message();
				msg.obj = "客户端已经连接上！可以发送信息。";
				msg.what = STATUS_CONNECT_SUCCESS;
				mHandler.sendMessage(msg);
				// 启动接受数据
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	// 客户端线程
	private class ClientThread extends Thread {
		public void run() {
			try {
				mSocket = mDevice
						.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
				Message msg = new Message();
				msg.obj = "请稍候，正在连接服务器:" + bluetoothAddr;
				msg.what = STATUS_CONNECT_SUCCESS;
				mHandler.sendMessage(msg);

				mSocket.connect();

				msg = new Message();
				msg.obj = "已经连接上服务端！可以发送信息。";
				msg.what = STATUS_CONNECT_SUCCESS;
				mHandler.sendMessage(msg);
				mReadThread = new ReadThread();
				mReadThread.start();
			} catch (IOException e) {
				Message msg = new Message();
				msg.obj = "连接服务端异常！断开连接重新试一试。";
				msg.what = STATUS_CONNECT_SUCCESS;
				mHandler.sendMessage(msg);
				CloseIO.close(mSocket);
			}
		}
	};

	// 读取数据
	private class ReadThread extends Thread {
//		private String s;

		public void run() {
			byte[] buffer = new byte[1024];
//			char[] buffer1 = new char[1024];
			int len;
			BufferedReader bufReader = null;
			InputStream is = null;
			try {
				// bufReader = new BufferedReader(new
				// InputStreamReader(mSocket.getInputStream()));
				// while (true) {
				// while ((len = bufReader.read(buffer1) )!=-1) {
				// s = new String(buffer1, 0, len);
				// }
				// Message msg = new Message();
				// msg.obj = s;
				// msg.what = 1;
				// mHandler.sendMessage(msg);
				// }

				is = mSocket.getInputStream();
				while (true) {
					if ((len = is.read(buffer)) > 0) {
						byte[] buf_data = new byte[len];
						for (int i = 0; i < len; i++) {
							buf_data[i] = buffer[i];
						}
						String s = new String(buf_data);
						Message msg = new Message();
						msg.obj = s;
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				CloseIO.close(is, bufReader);
			}

		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String info = (String) msg.obj;
			switch (msg.what) {
			case STATUS_CONNECT_SUCCESS:
				Toast.makeText(ChatActivity.this, info, Toast.LENGTH_SHORT).show();
				break;
			case SET_TITLE :
				setTitle(info);
				break;
			}

			if (msg.what != 1) {
				chatContent = new Bluetooth(R.drawable.wearable, "me", info);
				arrayList.add(chatContent);
				chatAdapter.notifyDataSetChanged();
				chat_list.setSelection(arrayList.size() - 1);
			} else {
				chatContent = new Bluetooth(R.drawable.game, bluetoothName, info);
				arrayList.add(chatContent);
				chatAdapter.notifyDataSetChanged();
				chat_list.setSelection(arrayList.size() - 1);
			}
		}

	};

	/* 停止服务器 */
	private void shutdownServer() {
		new Thread() {
			public void run() {
				if (mServerThread != null) {
					mServerThread.interrupt();
					mServerThread = null;
				}
				if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
				}
				CloseIO.close(mSocket, mServerSocket);
			};
		}.start();
	}

	/* ͣ停止客户端连接 */
	private void shutdownClient() {
		new Thread() {
			public void run() {
				if (mClientThread != null) {
					mClientThread.interrupt();
					mClientThread = null;
				}
				if (mReadThread != null) {
					mReadThread.interrupt();
					mReadThread = null;
				}
				CloseIO.close(mSocket);
			};
		}.start();
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (servicflag) {
			shutdownServer();
		} else {
			shutdownClient();
		}
		isOpen = false;
		
	}
}
