import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

public class Cliente {

    public String id;
    private String name;
    private String senha;

    private Conta conta;

    private SecretKey chaveSimetrica;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public SecretKey getChaveSimetrica() {
        return chaveSimetrica;
    }

    public void setChaveSimetrica(SecretKey chaveSimetrica) {
        this.chaveSimetrica = chaveSimetrica;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }
     String enviarRequisicao(String request){
        return Criptografar(request);
    }

    Cliente(String name, String senha, SecretKey chave){
        this.name = name;
        this.senha = senha;
        this.chaveSimetrica = chave;
    }
    JSONObject receberResponse(String request) throws ParseException {
        String cod= Descriptografar(request);
        JSONParser jsonParser = new JSONParser();
        JSONObject dados = (JSONObject) jsonParser.parse(cod);
        return dados;
    }
    private String Criptografar(String mensagem) {
        try {
            // Criptografar a mensagem com AES
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, this.getChaveSimetrica());
            byte[] mensagemCriptografada = cipher.doFinal(mensagem.getBytes(StandardCharsets.UTF_8));

            // Codificar a mensagem em base64
            return Base64.getEncoder().encodeToString(mensagemCriptografada);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public  String Descriptografar(String mensagemCriptografada) {
        try {
            // Decodificar a mensagem da base64
            byte[] mensagemBase64 = Base64.getDecoder().decode(mensagemCriptografada);

            // Descriptografar a mensagem com AES
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, this.getChaveSimetrica());
            byte[] mensagemDescriptografada = cipher.doFinal(mensagemBase64);

            // Exibir a mensagem decodificada
            String mensagem = new String(mensagemDescriptografada, StandardCharsets.UTF_8);
            return mensagem;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
