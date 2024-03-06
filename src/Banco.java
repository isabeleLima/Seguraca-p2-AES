import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Banco {
    private List<Cliente> clientes = new ArrayList<>();

    Banco(){
        Cliente josefina = this.criarCliente("Josefina", "123");
        this.criarConta(josefina, "123456", "88888888", "aquele la");

        Cliente pauloProducoes = this.criarCliente("Paulo", "123");
        this.criarConta(pauloProducoes, "000000", "88888888", "aquele la");

        Cliente MariaBonita = this.criarCliente("Maria", "123");
        this.criarConta(MariaBonita, "112233", "88888888", "aquele la");

    }
    public static SecretKey generateKey(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            return keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String VigenereEncrypt(String plainText, String key) {
        StringBuilder cipherText = new StringBuilder();
        int keyLength = key.length();

        for (int i = 0; i < plainText.length(); i++) {
            char plainChar = plainText.charAt(i);
            char keyChar = key.charAt(i % keyLength);

            // Ignora caracteres não alfabéticos no texto original
            if (Character.isLetter(plainChar)) {
                char encryptedChar = encryptChar(plainChar, keyChar);
                cipherText.append(encryptedChar);
            } else {
                cipherText.append(plainChar);
            }
        }

        return Base64.getEncoder().encodeToString(cipherText.toString().getBytes());
    }

    private static char encryptChar(char plainChar, char keyChar) {
        int base = Character.isUpperCase(plainChar) ? 'A' : 'a';
        int encryptedChar = (plainChar + keyChar - 2 * base) % 26 + base;
        return (char) encryptedChar;
    }
    void adicionarCliente(Cliente cliente){
        Cliente cl = new Cliente(cliente.getName(),cliente.getSenha(),generateKey());
        cl.setId(cliente.getId());
        clientes.add(cl);
    }

    public void autualizarContaClient(Cliente cliente){
        clientes.stream()
                .filter(item -> item.getId() == cliente.getId())
                .findFirst()
                .ifPresent(item -> item.setConta(cliente.getConta()));
    }

    public void autualizarSaldoClient(Cliente cliente, double saldo){
        clientes.stream()
                .filter(item -> item.getId() == cliente.getId())
                .findFirst()
                .ifPresent(item -> item.getConta().setSaldo(saldo));
    }

    public Cliente autenticar(String senha, String nome){
        Cliente cl = clientes.stream()
                .filter(item -> item.getSenha().equals(VigenereEncrypt(senha, "ChaveSuperSecreta"))
                        && item.getName().equals(nome))
                .findFirst()
                .orElse(null); // Retorna null se o item não existe
        if(cl != null){
            return cl;
        }
        return null;
    }

    public Cliente criarCliente(String name, String senha){
        Cliente cliente = new Cliente(name,VigenereEncrypt(senha,"ChaveSuperSecreta"), null);
        cliente.setId(UUID.randomUUID().toString());
        this.adicionarCliente(cliente);
        return cliente;

    }

    public void imprimir(){
        for(Cliente cl : clientes){
            System.out.println(cl.getName());
            System.out.println(cl.getSenha());
            System.out.println(cl.getChaveSimetrica());
            System.out.println("-------------------------------------------");
        }
    }

    String enviarResponse(String request, SecretKey chave){
        return Criptografar(request, chave);
    }

    String receberRequest(String request, String json, String id){
            try {
                //como ele está tentando entrar n ha criptografia aq alem da usada na senha
                if(request.equals("auth")){
                    JSONParser jsonParser = new JSONParser();
                    JSONObject dados = (JSONObject) jsonParser.parse(json);

                   Cliente cl = this.autenticar(dados.get("senha").toString(), dados.get("nome").toString());
                   JSONObject jsonResponse = new JSONObject();

                   byte[] encodedKey = cl.getChaveSimetrica().getEncoded();
                   jsonResponse.put("id", cl.getId());
                   jsonResponse.put("name", cl.getName());
                   jsonResponse.put("chave",  Base64.getEncoder().encodeToString(encodedKey));

                   if(getContabyClientId(cl.getId()) != null){
                       jsonResponse.put("hasConta",  "Sim");
                   }else{
                       jsonResponse.put("hasConta",  "Nao");
                   }

                   return jsonResponse.toJSONString();
                }else if(request.equals("creatCliente")){
                    JSONParser jsonParser = new JSONParser();
                    JSONObject dados = (JSONObject) jsonParser.parse(json);

                    String name = dados.get("nome").toString();
                    String senha = dados.get("senha").toString();

                    this.criarCliente(name,senha);
                }else{
                    Cliente client = getClient(id);
                    String req = Descriptografar(json, client.getChaveSimetrica());

                    JSONParser jsonParser = new JSONParser();
                    JSONObject dados = (JSONObject) jsonParser.parse(req);

                    String type = dados.get("endpoint").toString();

                    switch (type) {
                        case "criar conta": {
                            String cpf = dados.get("cpf").toString();
                            String telefone = dados.get("telefone").toString();
                            String endereco = dados.get("endereco").toString();

                            this.criarConta(client, cpf, telefone, endereco);

                            JSONObject jsonResponse = new JSONObject();
                            jsonResponse.put("message", "conta criada com sucesso!");
                            return this.enviarResponse(jsonResponse.toJSONString(), client.getChaveSimetrica());
                        }
                        case "saldo": {
                            JSONObject jsonResponse = new JSONObject();
                            jsonResponse.put("message", "seu saldo é: " + client.getConta().getSaldo());
                            return this.enviarResponse(jsonResponse.toJSONString(), client.getChaveSimetrica());
                        }
                        case "deposito": {
                            double novoSaldo = client.getConta().getSaldo() +  Double.parseDouble(dados.get("valor").toString());
                            autualizarSaldoClient(client, novoSaldo);
                            JSONObject jsonResponse = new JSONObject();
                            jsonResponse.put("message", "Deposito realizado com sucesso");
                            return this.enviarResponse(jsonResponse.toJSONString(), client.getChaveSimetrica());
                        }
                        case "saque": {
                            double novoSaldo = client.getConta().getSaldo() -  Double.parseDouble(dados.get("valor").toString());
                            JSONObject jsonResponse = new JSONObject();
                            if(novoSaldo < 0){
                                jsonResponse.put("message", "Saldo insuficiente para realizar o saque");
                            }else{
                                autualizarSaldoClient(client, novoSaldo);
                                jsonResponse.put("message", "Saque realizado com sucesso");
                            }
                            return this.enviarResponse(jsonResponse.toJSONString(), client.getChaveSimetrica());
                        }
                        case "transferencia": {
                            double novoSaldo = client.getConta().getSaldo() -  Double.parseDouble(dados.get("valor").toString());
                            JSONObject jsonResponse = new JSONObject();
                            if(novoSaldo < 0){
                                jsonResponse.put("message", "Saldo insuficiente para realizar transferencia");
                            }else{
                                Cliente destino = getClientbyCpf(dados.get("cpf").toString());
                                if(destino == null){
                                    jsonResponse.put("message", "Cliente não encontrado verifique se o cpf foi digitado corretamente");
                                }else{
                                    autualizarSaldoClient(client, novoSaldo);
                                    novoSaldo = destino.getConta().getSaldo() +  Double.parseDouble(dados.get("valor").toString());
                                    autualizarSaldoClient(destino, novoSaldo);
                                    jsonResponse.put("message", "Transferencia realizada com sucesso!");
                                }
                            }
                            return this.enviarResponse(jsonResponse.toJSONString(), client.getChaveSimetrica());
                        }
                    }
                }

            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    private String Criptografar(String mensagem, SecretKey chave) {
        try {
            // Criptografar a mensagem com AES
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, chave);
            byte[] mensagemCriptografada = cipher.doFinal(mensagem.getBytes(StandardCharsets.UTF_8));

            // Codificar a mensagem em base64
            return Base64.getEncoder().encodeToString(mensagemCriptografada);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public  String Descriptografar(String mensagemCriptografada, SecretKey chave) {
        try {
            // Decodificar a mensagem da base64
            byte[] mensagemBase64 = Base64.getDecoder().decode(mensagemCriptografada);

            // Descriptografar a mensagem com AES
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, chave);
            byte[] mensagemDescriptografada = cipher.doFinal(mensagemBase64);

            // Exibir a mensagem decodificada
            String mensagem = new String(mensagemDescriptografada, StandardCharsets.UTF_8);
            return mensagem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void criarConta(Cliente cliente, String cpf, String telefone, String endereco){
        Conta conta = new Conta(UUID.randomUUID().toString(), 0, cliente, cpf, telefone, endereco);
        cliente.setConta(conta);
        this.autualizarContaClient(cliente);
    }

    Cliente getClient(String id){
        return clientes.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null); // Retorna null se o item não existe
    }
    Cliente getClientbyCpf(String cpf){
        return clientes.stream()
                .filter(item -> item.getConta().getCpf().equals(cpf))
                .findFirst()
                .orElse(null); // Retorna null se o item não existe
    }
    Conta getContabyClientId(String id){
        Cliente cl = clientes.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null); // Retorna null se o item não existe
        if(cl != null){
            return cl.getConta();
        }
        return null;
    }
}
