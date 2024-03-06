import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Scanner;

public class Main {
    static String key = "ChaveSuperSecreta";
    public static  void main(String[] args) throws ParseException {
        Banco banco = new Banco();
        Cliente clienteDaVez = null;
        Scanner entrada = new Scanner(System.in);
        String option = null;
        String menu;
        while(true){
            if(clienteDaVez == null){
                System.out.println("1- criar conta");
                System.out.println("2- auth");
                System.out.println("3- encerrar consulta");
                option = entrada.nextLine();

                switch(option){
                    case "1":{
                        JSONObject jsonObject = new JSONObject();
                        System.out.println("escreva o nome");
                        jsonObject.put("nome",entrada.nextLine());
                        System.out.println("escreva a senha");
                        jsonObject.put("senha",entrada.nextLine());

                        banco.receberRequest("creatCliente", jsonObject.toJSONString(), "0");
                        System.out.println("cliente Cadastrado com sucesso!");
                        break;
                    }
                    case "2":{
                        JSONObject jsonObject = new JSONObject();
                        clienteDaVez = new Cliente("","", null);
                        System.out.println("escreva o nome");
                        jsonObject.put("nome",entrada.nextLine());
                        System.out.println("escreva a senha");
                        jsonObject.put("senha",entrada.nextLine());

                        String response = banco.receberRequest("auth", jsonObject.toJSONString(), "0");

                        JSONParser jsonParser = new JSONParser();
                        JSONObject jsonResponse = (JSONObject) jsonParser.parse(response);

                        String chave = jsonResponse.get("chave").toString();
                        byte[] decodedKey = Base64.getDecoder().decode(chave);


                        clienteDaVez.setId(jsonResponse.get("id").toString());
                        clienteDaVez.setName(jsonResponse.get("name").toString());
                        clienteDaVez.setChaveSimetrica(new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES"));

                        if(jsonResponse.get("hasConta").toString().equals("Nao")){
                            newConta(clienteDaVez, banco);
                        }
                        break;
                    }case "3":{
                        System.out.println("encerrando o sistema");
                        break;
                    }
                    case "4":{
                        banco.imprimir();
                        break;
                    }
                }
            }else{
                System.out.println("0- Criar Conta");
                System.out.println("1- Saldo");
                System.out.println("2- deposito");
                System.out.println("3- Saque");
                System.out.println("4- tranferencia");
                System.out.println("5- Sair");
                menu = entrada.nextLine();

                switch(menu){
                    case "1":{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("endpoint", "saldo");

                        String request = clienteDaVez.enviarRequisicao(jsonObject.toJSONString());
                        String response = banco.receberRequest(" ", request, clienteDaVez.getId());
                        jsonObject = clienteDaVez.receberResponse(response);

                        System.out.println(jsonObject.get("message"));
                        break;
                    }
                    case "2":{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("endpoint", "deposito");

                        System.out.println("digite o valor desejado para deposito");
                        jsonObject.put("valor",entrada.nextLine());

                        String request = clienteDaVez.enviarRequisicao(jsonObject.toJSONString());
                        String response = banco.receberRequest(" ", request, clienteDaVez.getId());
                        jsonObject = clienteDaVez.receberResponse(response);

                        System.out.println(jsonObject.get("message"));
                        break;
                    }
                    case "3":{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("endpoint", "saque");

                        System.out.println("digite o valor desejado para saque");
                        jsonObject.put("valor",entrada.nextLine());

                        String request = clienteDaVez.enviarRequisicao(jsonObject.toJSONString());
                        String response = banco.receberRequest(" ", request, clienteDaVez.getId());
                        jsonObject = clienteDaVez.receberResponse(response);
                        System.out.println(jsonObject.get("message"));
                        break;
                    }
                    case "4":{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("endpoint", "transferencia");

                        System.out.println("digite o valor desejado para tranferencia");
                        jsonObject.put("valor",entrada.nextLine());

                        System.out.println("digite o cpf da conta que recebera a transferencia");
                        jsonObject.put("cpf",entrada.nextLine());

                        String request = clienteDaVez.enviarRequisicao(jsonObject.toJSONString());
                        String response = banco.receberRequest(" ", request, clienteDaVez.getId());
                        jsonObject = clienteDaVez.receberResponse(response);
                        System.out.println(jsonObject.get("message"));
                        break;
                    }
                    case "5":{
                       clienteDaVez = null;
                        break;
                    }
                }
            }
            if (option.equals("3")) {
                break;
            }
        }

    }
    public static void newConta(Cliente clienteDaVez, Banco banco) throws ParseException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("endpoint", "criar conta");
        Scanner entrada = new Scanner(System.in);
        System.out.println("Você n tem um conta corrente vamos criar uma");
        System.out.println("Digite seu cpf");
        jsonObject.put("cpf", entrada.nextLine());
        System.out.println("Digite seu telefone");
        jsonObject.put("telefone", entrada.nextLine());
        System.out.println("Digite seu endereco");
        jsonObject.put("endereco", entrada.nextLine());

        String request = clienteDaVez.enviarRequisicao(jsonObject.toJSONString());
        String response = banco.receberRequest(" ", request, clienteDaVez.getId());
        jsonObject = clienteDaVez.receberResponse(response);

        System.out.println(jsonObject.get("message"));
    }
}