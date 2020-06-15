import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExchangeWithServer{
    //метод для перечисления всех файлов сервера которые можно запросить и выгрузить
    public static void allFiles(Socket socket) throws IOException {
        System.out.println("===================Start===================");
        DataInputStream inp = new DataInputStream(socket.getInputStream());
        System.out.println(inp.readUTF());
        System.out.println("===================End===================");
    }
    public static boolean approve(Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        System.out.println("Send to server? Enter \"yes\" if the answer is yes or something else to cancel " +
                "and start again.");
        Scanner console = new Scanner(System.in);
        String command = console.nextLine();
        boolean a;
        if (command.equals("yes")) {
            System.out.println("///Done///");
            out.writeUTF(command);
            a = true;
        }
        else {
            System.out.println("|||Cancel|||");
            out.writeUTF(command);
            a = false;
        }
        return a;
    }

    //метод для передачи файла серверу.
    static void upload(Socket socket) throws IOException {
        System.out.println("===================Start===================");

        String path;
        File file;
        //запрашиваем у клиента путь к файлу на его пк
        System.out.println("\nEnter the path to the file on your computer, or enter \"exit\" to return to the menu");
        Scanner console1 = new Scanner(System.in);
        path = console1.nextLine();

        //если клиент решил выйти из аплода - выходим и приказываем серверу
        DataOutputStream exit = new DataOutputStream(socket.getOutputStream());
        if (path.equals("exit")) {
            System.err.println("Go back to the menu");
            exit.writeBoolean(true);
            return;
        }
        //объявляем путь
        file = new File(path);

        InputStream in;
        OutputStream out = socket.getOutputStream();
        try {
            in = new FileInputStream(file);
        } catch (Exception e) {
            System.out.println("\nInvalid file path: " + e);
            //если путь неверный - запускаем аплод заново
            ExchangeWithServer.upload(socket);
            return;
        }
        //даем серверу знать, что путь настоящий и клиент хочет продолжить эту сессию загрузки
        exit.writeBoolean(false);

        //имя шаблона
        System.out.println("Enter a name for this file under which it will be stored on the server (without extension).\n"+
                "To return to the menu, enter \"exit\"");

        //берем расширение файла регулярным выражением
        Pattern pattern = Pattern.compile("\\.[^.]*$");
        Matcher matcher = pattern.matcher(path);
        String fileExtension = null;
        while(matcher.find()){
            fileExtension = path.substring(matcher.start(), matcher.end());
        }

        // Создаем поток для чтения с клавиатуры.
        //создаем имя с расширением
        Scanner console = new Scanner(System.in);
        String name = console.nextLine();

        DataOutputStream exit1 = new DataOutputStream(socket.getOutputStream());
        //если клиент решил выйти из аплода - выходим и приказываем серверу
        if (name.equals("exit")) {
            System.err.println("Go back to the menu");
            exit1.writeBoolean(true);
            return;
        } else {
            exit1.writeBoolean(false);
        }
        System.out.println("File name: \"" + name + fileExtension + "\"");

        //спрашиваем аппрув у клиента
        if (ExchangeWithServer.approve(socket)) {
            //отправляем имя серверу
            DataOutputStream outN = new DataOutputStream(socket.getOutputStream());
            outN.writeUTF(name + fileExtension);
            //отправляем размер файла серверу
            long lenghtF = file.length();
            DataOutputStream outL = new DataOutputStream(socket.getOutputStream());
            outL.writeLong(lenghtF);

            //передаем байты серверу
            byte[] bytes = new byte[(int) lenghtF];
            while (in.read(bytes) > 0) {
                out.write(bytes);
            }

            //сообщение сервера о приеме и записи файла
            DataInputStream inpM = new DataInputStream(socket.getInputStream());
            String line = inpM.readUTF();
            System.out.println(line);
            System.out.println("===================End===================");

        } else {
            ExchangeWithServer.upload(socket);
        }
    }

    //метод для выгрузки файла из сервера в фс клиента
    static void getFile(Socket socket) throws IOException {
        System.out.println("===================Start===================");
        System.out.println("getTemplateImage");

        Scanner console = new Scanner(System.in);
        //имя запрашиваемого файла с сервера
        System.out.println("Enter the name of the requested file (with extension), " +
                "or enter \"exit\" to return to the menu.");
        String name = console.nextLine();

        //если клиент решил выйти из гета - выходим и приказываем серверу
        DataOutputStream exit = new DataOutputStream(socket.getOutputStream());
        if (name.equals("exit")) {
            System.err.println("Go back to the menu");
            exit.writeBoolean(true);
            return;
        }
        exit.writeBoolean(false);

        try {
            //Отсылаем серверу имя нужного файла
            DataOutputStream outN = new DataOutputStream(socket.getOutputStream());
            outN.writeUTF(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //проверяем нахождение файла на сервере
        DataInputStream err = new DataInputStream(socket.getInputStream());
        if(err.readBoolean()){
            System.out.println("This file is not on the server");
            ExchangeWithServer.getFile(socket);
            return;
        }
        System.out.println("File is available");

        //куда заливать файл с сервера
        System.out.println("Enter the path where to upload the file from the server, " +
                "or enter \"exit\" to return to the menu.");

        String path = console.nextLine();

        //если клиент решил выйти из гета - выходим и приказываем серверу
        DataOutputStream exit1 = new DataOutputStream(socket.getOutputStream());
        if (path.equals("exit")) {
            System.err.println("Go back to the menu");
            exit1.writeBoolean(true);
            return;
        }
        exit1.writeBoolean(false);

        File filePath;
        try {
            InputStream in = socket.getInputStream();
            OutputStream out;
            try {
                //путь куда попадет файл клиента
                filePath = new File(path);
                out = new FileOutputStream(filePath + "\\" + name);
            } catch (FileNotFoundException e) {
                System.out.println("Invalid file path: " + e);
                return;
            }
            //принимаем от сервера размер файла
            long lenghtF;
            DataInputStream inpL = new DataInputStream(socket.getInputStream());
            lenghtF = inpL.readLong();

            //записываем
            byte[] bytes = new byte[(int) lenghtF];
            int count;
            assert in != null;
            ArrayList<Integer> arr = new ArrayList<>();
            while (((count = in.read(bytes)) > 0)) {
                out.write(bytes, 0, count);
                arr.add(count);
                int sum = 0;
                for (int d : arr) sum += d;
                if (sum >= lenghtF) {
                    break;
                }
            }
            out.close();
            System.out.println("File in the system. Path: " + filePath + "\\" + name);
            System.out.println("===================End===================");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
