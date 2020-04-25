package fr.azuxul.ibustransmitter;

import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<ConnectTask.ConnectTaskArguments, Object, Client> {

    private Client client;

    @Override
    protected Client doInBackground(ConnectTaskArguments... connectTaskArguments) {

        client = new Client(connectTaskArguments[0].getIp(), connectTaskArguments[0].getPort());

        client.start();

        return null;
    }

    public Client getClient() {
        return client;
    }

    public static class ConnectTaskArguments {

        private String ip;
        private int port;

        public ConnectTaskArguments(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
