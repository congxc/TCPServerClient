package com.rain.shieh.tcpserverclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mTvIp;
    private TextView mTvPort;
    private RecyclerView mSendRecyclerView;
    private RecyclerView mReceiveRecyclerView;
    private EditText mEtMessage;
    private Button mButton;
    private Toolbar mToolbar;

    private long lastClickTime = 0;

    private boolean mIsServer = true;//0 服务端 1客户端
    private boolean mIsTcp = true;

    private List<Message> mSendMessages = new ArrayList<>();
    private List<Message> mReceivedMessages = new ArrayList<>();

    private BaseQuickAdapter<Message,BaseViewHolder> mSendAdapter;
    private BaseQuickAdapter<Message,BaseViewHolder> mReceivedAdapter;

    private Socket mSocket;//TCP客户端
    private ServerSocket mServerSocket;//TCP服务端
    private Socket mServerSocketClient;//TCP服务端获取的客户端
    private InputStream mServerInputStream;//服务端接受消息
    private OutputStream mServerOutputStream;//服务端发送消息通道
    private InputStream mClientSocketInputStream;//客户端接受消息
    private OutputStream mClientOutputStream;//客户端发送消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolBar();
        initView();
        initListener();
    }

    private void initListener() {
        mButton.setOnClickListener(this);
    }

    private void initToolBar() {
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        MenuItem itemServer = menu.findItem(R.id.menu_server);
        itemServer.setTitle(mIsServer?R.string.server:R.string.client);
        itemServer.setChecked(mIsServer);
        MenuItem itemTcp = menu.findItem(R.id.menu_tcp);
        itemTcp.setTitle(mIsTcp?R.string.TCP:R.string.UDP);
        itemTcp.setChecked(mIsTcp);
        mTvIp.setEnabled(!mIsServer);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(mIsServer?R.string.server:R.string.client);
        if(mIsServer && !mIsTcp){
            StartUDPServer();
        }
        initState();
    }

    private void initState() {
        if (!mIsTcp) {
            mButton.setText(R.string.send);
            if (mIsServer) {
                StartUDPServer();
            }
        }else{
            if (mIsServer) {
                if (mServerSocket == null) {
                    mButton.setText(R.string.start_server);
                }else{
                    mButton.setText(R.string.send);
                }

            }else{
                if (mSocket == null || !mSocket.isConnected()) {
                    mButton.setText(R.string.connect);
                }
            }
        }
    }

    private void initView() {
        mIsServer = SharedPreferencesdUtil.getBoolean(this,"server",true);
        mIsTcp = SharedPreferencesdUtil.getBoolean(this,"tcp",true);
        mTvIp = (EditText)findViewById(R.id.server_ip);
        mTvPort = (TextView)findViewById(R.id.server_port);
        mSendRecyclerView = (RecyclerView)findViewById(R.id.send_recyclerView);
        mReceiveRecyclerView = (RecyclerView) findViewById(R.id.receive_recyclerView);
        mEtMessage = (EditText) findViewById(R.id.et_message);
        mButton = (Button) findViewById(R.id.btn_send);
        String ipAddress = NetworkUtils.getIPAddress(true);
        mTvIp.setText(ipAddress);
        mSendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mReceiveRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSendAdapter = new BaseQuickAdapter<Message, BaseViewHolder>(R.layout.item_layout,mSendMessages) {
            @Override
            protected void convert(BaseViewHolder helper, Message item) {
                helper.setText(R.id.tv_content,item.content);
            }
        };
        mSendRecyclerView.setAdapter(mSendAdapter);
        mReceivedAdapter = new BaseQuickAdapter<Message, BaseViewHolder>(R.layout.item_layout,mReceivedMessages) {
            @Override
            protected void convert(BaseViewHolder helper, Message item) {
                helper.setText(R.id.tv_content,item.content);
            }
        };
        mReceiveRecyclerView.setAdapter(mReceivedAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case  R.id.menu_server:
                mIsServer = !item.isChecked();
                SharedPreferencesdUtil.putBoolean(this,"server",mIsServer);
                item.setTitle(mIsServer?R.string.server:R.string.client);
                item.setChecked(mIsServer);
                setTitle(mIsServer?R.string.server:R.string.client);
                mTvIp.setEnabled(!mIsServer);
                if (mIsServer) {
                    String ipAddress = NetworkUtils.getIPAddress(true);
                    mTvIp.setText(ipAddress);
                }
                initState();
                mSendMessages.clear();
                mSendAdapter.notifyDataSetChanged();
                mReceivedMessages.clear();
                mReceivedAdapter.notifyDataSetChanged();
                close();
                break;
            case  R.id.menu_tcp:
                mIsTcp = !item.isChecked();
                SharedPreferencesdUtil.putBoolean(this,"tcp",mIsTcp);
                item.setTitle(mIsTcp?R.string.TCP:R.string.UDP);
                item.setChecked(mIsTcp);
                initState();
                mSendMessages.clear();
                mSendAdapter.notifyDataSetChanged();
                mReceivedMessages.clear();
                mReceivedAdapter.notifyDataSetChanged();
                close();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (!checkSingClick()) {
            return;
        }
        if (R.id.btn_send == v.getId()) {
            if (!mIsServer) {//客户端发送消息
                if(mIsTcp){
                    TCPClientSendMessage();
                }else {
                    UDPClient();
                }
            }else{
                if (mIsTcp) {//服务端发送消息
                    TCPServerSendMessage();
                }
            }
        }
    }

    private boolean checkSingClick(){
        if (System.currentTimeMillis() - lastClickTime < 500) {
            lastClickTime = System.currentTimeMillis();
            return false;
        }
        lastClickTime = System.currentTimeMillis();
        return true;
    }

    /**
     * TCP服务端发送消息
     */
    private void TCPServerSendMessage() {
        if (mServerOutputStream == null) {
            TCPServerStart();
            return;
        }
        ThreadPoolCreator.get().getFixedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String s = mEtMessage.getText().toString();
                    if (TextUtils.isEmpty(s)) {
                        return;
                    }
                    Message message = new Message();
                    message.content = s;
                    addSendMessage(message);
                    byte[] bytes = s.getBytes();
                    mServerOutputStream.write(bytes);
                    mServerOutputStream.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 开启TCP服务器
     */
    private void TCPServerStart() {
        ThreadPoolCreator.get().getFixedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mServerSocket == null) {
                        mServerSocket = new ServerSocket(Integer.parseInt(mTvPort.getText().toString()));
                        mServerSocketClient = mServerSocket.accept();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButton.setText(R.string.send);
                        }
                    });
                    //获取发送消息通道 写消息发送给客户端
                    mServerOutputStream = mServerSocketClient.getOutputStream();
                    //获取收到的消息
                    mServerInputStream = mServerSocketClient.getInputStream();

                    byte[] buf = new byte[1024];
                    int len = 0;
                    while (mServerInputStream != null){
                        len = mServerInputStream.read(buf);
                        String msg = new String(buf,0,len);
                        Message message = new Message();
                        message.content = msg;
                        addReceivedMessage(message);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * TCP客户端连接服务器
     */
    private void TCPClientConnect(){
        ThreadPoolCreator.get().getFixedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mSocket == null) {
                        mSocket = new Socket();
                    }
                    if (!mSocket.isConnected()) {
                        String host = mTvIp.getText().toString();
                        if (TextUtils.isEmpty(host)) {
                            ToastUtils.showLong("请输入服务器IP地址");
                            return;
                        }
                        SocketAddress remoteAddr = null;
                        remoteAddr = new InetSocketAddress(InetAddress.getByName(host),
                                Integer.parseInt(mTvPort.getText().toString()));
                        mSocket.connect(remoteAddr);
                        Message message = new Message();
                        if (mSocket.isConnected()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mButton.setText(R.string.send);
                                }
                            });
                            ToastUtils.showLong("连接成功");
                            message.conState = true;
                            message.content = "连接成功";
                            addSendMessage(message);
                            //客户端接受消息
                            mClientSocketInputStream = mSocket.getInputStream();
                            byte[] buf = new byte[1024];
                            int len = 0;
                            while (mClientSocketInputStream != null){
                                len = mClientSocketInputStream.read(buf);
                                if (len > 0) {
                                    String msg = new String(buf,0,len);
                                    Message message2 = new Message();
                                    message2.content = msg;
                                    addReceivedMessage(message2);
                                }
                            }

                        }else{
                            message.conState = true;
                            message.content = "连接失败";
                            ToastUtils.showLong("连接失败");
                            addSendMessage(message);
                        }
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * TCP客户端发送消息
     */
    private void TCPClientSendMessage(){
         if(mSocket == null || !mSocket.isConnected()){
             TCPClientConnect();
             return;
         }
         ThreadPoolCreator.get().getFixedThreadPool().submit(new Runnable() {
             @Override
             public void run() {
                 try {
                     if (mSocket.isConnected()) {
                         if (mClientOutputStream == null) {
                                mClientOutputStream = mSocket.getOutputStream();
                         }
                         String message = mEtMessage.getText().toString();
                         if (!TextUtils.isEmpty(message)) {
                             mClientOutputStream.write((message).getBytes());
                             mClientOutputStream.flush();
                             Message msg = new Message();
                             msg.content = message;
                             addSendMessage(msg);
                         }
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         });
    }

    private void addSendMessage(final Message message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendMessages.add(message);
                mSendAdapter.notifyItemInserted(mSendMessages.size() - 1);
                mSendRecyclerView.scrollToPosition(mSendMessages.size() - 1);
            }});
    }
    private void addReceivedMessage(final Message message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mReceivedMessages.add(message);
                mReceivedAdapter.notifyItemInserted(mReceivedMessages.size() - 1);
                mReceiveRecyclerView.scrollToPosition(mReceivedMessages.size() - 1);
            }});
    }
    private void StartUDPServer(){
        ThreadPoolCreator.get().getFixedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket datagramSocket = new DatagramSocket(4000);
                    byte[] data = new byte[1024];
                    DatagramPacket pack = new DatagramPacket(data, data.length);
                    datagramSocket.receive(pack);
                    String hostAddress = pack.getAddress().getHostAddress();
                    int port = pack.getPort();
                    String message = new String(data);
                    LogUtils.info("cong.xie", "StartUDPServer: 收到消息 "+hostAddress+":"+port + " -> " +message);
                    datagramSocket.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void UDPClient(){
        final String[] message = {mEtMessage.getText().toString()};
        if (TextUtils.isEmpty(message[0])) {
            return;
        }
        final String host = mTvIp.getText().toString();
        if (TextUtils.isEmpty(host)) {
            ToastUtils.showLong("请输入服务器IP地址");
            return;
        }
        ThreadPoolCreator.get().getFixedThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket datagramSocket = new DatagramSocket(4000);
                    message[0] = "UDP发送消息:" + message[0];
                    DatagramPacket packet = new DatagramPacket(message[0].getBytes(), 0, message[0].getBytes().length,
                            InetAddress.getByName(host), Integer.parseInt(mTvPort.getText().toString()));
                    datagramSocket.send(packet);
                    datagramSocket.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private class Message{
//        String title;
        String content;
        boolean conState;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }

    private void close(){
        try {
            if(mClientOutputStream != null){
                mClientOutputStream.close();
                mClientOutputStream = null;
            }
            if (mClientSocketInputStream != null) {
                mClientSocketInputStream.close();
                mClientSocketInputStream = null;
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            if (mServerOutputStream != null) {
                mServerOutputStream.close();
                mServerOutputStream = null;
            }
            if (mServerInputStream != null) {
                mServerInputStream.close();
                mServerInputStream = null;
            }
            if (mServerSocketClient != null) {
                mServerSocketClient.close();
                mServerSocketClient = null;
            }
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
