package proxy;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ningyu He on 2016/11/29.
 */
public class RequestThread implements Runnable {
    private Map<String, String> header;

    private BufferedInputStream clientInput;

    private Socket clientSocket;

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    private String url_for_sending;    //直接就是文件路径了

    private String requestMethod;

    private String HttpVersion;

    public RequestThread(Socket clientSocket) {
        header = new HashMap();
        this.clientSocket = clientSocket;
    }

    public void clientToProxy() throws IOException {
        StringBuilder request = new StringBuilder();

        File writename = new File("C:\\Users\\zbh\\Desktop\\request.txt");
        writename.createNewFile();
        BufferedWriter outrequest = new BufferedWriter(new FileWriter(writename));

        int tempReader;
        int lineNumber = 0;
        int isEndOfRequest = 0;
        //String requestMethod = new String();
        String fileURL = new String();
        //String HttpVersion = new String();
        String key = new String();
        String value = new String();
        String requestHeadline_2 = new String();

        while ((tempReader = clientInput.read()) != -1) {
            if ((char) (tempReader) == '\r' || (char) (tempReader) == '\n') {
                isEndOfRequest++;
                if (isEndOfRequest == 2) {
                    String requestHeadline = request.toString();
                    lineNumber += 1;
                    if (lineNumber == 1) {
                        outrequest.write(requestHeadline + "\r\n");
                        requestMethod = requestHeadline.split(" ")[0];
                        if (!requestMethod.toLowerCase().equals("get") && !requestMethod.toLowerCase().equals("post")){
                            requestMethod = null;
                            break;
                        }
                        fileURL = requestHeadline.split(" ")[1];
                        URL url = new URL(fileURL);
                        url_for_sending = url.getFile();
                        HttpVersion = requestHeadline.split(" ")[2];
                    }
                    if (lineNumber > 1) {
                        key = requestHeadline.split(":")[0].toLowerCase();
                        value = requestHeadline.split(":")[1].toLowerCase();
                        if(!key.equals("cookie") && !key.equals("accept-encoding") &&
                                !key.equals("proxy-connection") && !key.equals("user-agent")){
                            outrequest.write(requestHeadline + "\r\n");
                        }
                        header.put(key, value);
                        if(header.containsKey("cookie")) header.remove("cookie");
                        if(header.containsKey("accept-encoding")) header.remove("accept-encoding");
                        if(header.containsKey("proxy-connection")) header.remove("proxy-connection");
                        if(header.containsKey("user-agent")) header.remove("user-agent");
                    }
                    request.delete(0, request.length());
                }
                if (isEndOfRequest == 3){
                    outrequest.write("\r\n");
                    break;
                }
            } else {
                isEndOfRequest = 0;
                request.append((char) tempReader);
            }
        }

        if(requestMethod.toLowerCase().equals("post")) {
            int flag = 0;
            while ((tempReader = clientInput.read()) != -1){
                if ((char) (tempReader) == '\r' || (char) (tempReader) == '\n') {
                    flag += 1;
                    request.append((char) tempReader);
                }
                else {
                    flag = 0;
                    request.append((char) tempReader);
                }
                if (flag == 4) {
                    requestHeadline_2 = request.toString();
                    outrequest.write(requestHeadline_2);
                    break;
                }
            }
        }

        outrequest.flush();
        outrequest.close();

        System.out.println(requestMethod + " " + fileURL + " " + HttpVersion);
        for (Map.Entry<String, String> entry : header.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println(requestHeadline_2);                                     //测试代码

        /*while ((tempReader = clientInput.read()) != -1) {
            bos.write(tempReader);
        }*/
    }

    public void ProxyToServer() {

    }

    @Override
    public void run() {
        try {
            clientInput = new BufferedInputStream(clientSocket.getInputStream());

            clientToProxy();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
