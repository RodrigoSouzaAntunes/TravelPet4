package com.example.travelpet.activity.cadastro.cadastroDadosMotorista;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.travelpet.R;
import com.example.travelpet.activity.MainActivity;
import com.example.travelpet.classes.Motorista;
import com.example.travelpet.classes.Usuario;
import com.example.travelpet.config.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CadastroCrlvMotoristaActivity extends AppCompatActivity {

    // Variaveis usadas para pegar dados da Activity CadastroTipoUsuario
    String idUsuario, emailUsuario, nomeUsuario,
            sobrenomeUsuario, telefoneUsuario, tipoUsuario;

    // Variavel armazena a foto da carteira de motorista
    byte[] fotoCNH;

    // Variavel armazena a foto da perfil do motorista
    byte[] fotoMotorista;

    // Variavel armazena a foto do documento do veículo do motorista
    byte[] fotoCrlvMotorista;

    // requestCode = SELECAO_GALERIA = e um codigo para ser passado no requestCode
    private static final int SELECAO_GALERIA = 200;

    // Variável armazena a referência do Sotorage
    private StorageReference storageReference;
    private FirebaseAuth fireAuth;
    FirebaseUser fireUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_crlv_motorista);

        // Recuperando dados passados da Activity CadastroTipoUsuario
        Intent intent = getIntent();
        Usuario usuario = intent.getParcelableExtra("usuario");
        Motorista motorista = intent.getParcelableExtra("motorista");
         fireUser = FirebaseAuth.getInstance().getCurrentUser();

        //      Armazena os dados recuperados em uma um variável String
        // dados classe Usuario
        idUsuario           =   usuario.getId();
        emailUsuario        =   usuario.getEmail();
        nomeUsuario         =   usuario.getNome();
        sobrenomeUsuario    =   usuario.getSobrenome();
        telefoneUsuario     =   usuario.getTelefone();
        tipoUsuario         =   usuario.getTipoUsuario();

        // Dados classe Motorista
        fotoCNH             =   motorista.getFotoCNH();
        fotoMotorista       =   motorista.getFotoPerfilMotorista();

        // Recuperando Referência do Storage do Firebase
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();

    }

    // Evento executado pelo botão enviar
    public void enviarCrlvMotorista (View view) {

        // Seleciona a foto da galeria
        Intent i  = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if(i.resolveActivity(getPackageManager()) != null){
            startActivityForResult(i, SELECAO_GALERIA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK){

            try {

                // Recupera local da imagem selecionada
                Uri localImagemSelecionada = data.getData();

                // getContentResolver() = responsável por recupera conteúdo dentro do app android
                Bitmap imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if ( imagem != null){

                    // Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    // Tranforma a imagem em um array de Bytes e armazena na variável
                    fotoCrlvMotorista = baos.toByteArray();

                    Toast.makeText(CadastroCrlvMotoristaActivity.this,
                            "Sucesso ao selececionar a imagem",
                            Toast.LENGTH_SHORT).show();

                }
            } catch (IOException e) {
                e.printStackTrace(); // Caso ocorra um erro podemos ver aqui
            }
        }
    }

    // Evento Botão buttonFinalizarCrlvMotorista
    public void finalizarCadastroMotorista(View view){

        if ( fotoCrlvMotorista != null ) {

            // Cria uma referência para a classe Usuário e instância ela
            Usuario usuario = new Usuario();

            // Envia a dados para classe Usuário
            usuario.setId(idUsuario);
            usuario.setEmail(emailUsuario);
            usuario.setNome(nomeUsuario);
            usuario.setSobrenome(sobrenomeUsuario);
            usuario.setTelefone(telefoneUsuario);
            usuario.setTipoUsuario(tipoUsuario);

            // Chama método salvar da classe Usuário, responsável por salvar no firebase
             usuario.salvar();

            salvarFotoCNH();
            salvarFotoCrlvMotorista();
            salvarFotoPerfilMotorista();

            //startActivity(new Intent(this, MainActivity.class));

            Toast.makeText(CadastroCrlvMotoristaActivity.this,
                    "Sucesso ao cadastrar Usuário Motorista !",
                    Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(CadastroCrlvMotoristaActivity.this,
                    "Envie a foto do CRVL",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //          Métodos para salvamento no FireBase, de cada imagem passadas das Activitys

    // Método de salvamento FotoCNG ( Activity EnviarCNH )
    public void salvarFotoCNH(){

       /* // Salvar Imagem no FireBase cada child e uma pasta criada, no ultimo e nome da imagem
        StorageReference imagemRef = storageReference
                .child("motorista")
                .child(idUsuario)
                .child("documento de validacao")
                .child("CNH")
                .child(idUsuario+".CNH.JPEG");


        */

        // Salvar Imagem no FireBase cada child e uma pasta criada, no ultimo e nome da imagem
        StorageReference imagemRef = storageReference
                .child("motorista")
                .child(fireUser.getEmail())
                .child("documento de validacao")
                .child("CNH")
                .child(idUsuario+".CNH.JPEG");


        // Método para realmente salvar no Firebase
        // putBytes = para os dados da imagem em bytes
        UploadTask uploadTask = imagemRef.putBytes (fotoCNH);
        // Método exibe uma mensagem caso ocorra uma falha na transferencia da imagem
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(CadastroCrlvMotoristaActivity.this,
                        "Erro ao fazer upload da imagem CNH",
                        Toast.LENGTH_SHORT).show();

            }
            // método exibe uma mensagem caso tenha sucesso ao enviar
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                /*
                Toast.makeText(CadastroCrlvMotoristaActivity.this,
                        "Sucesso ao fazer upload da imagem",
                        Toast.LENGTH_SHORT).show();

                 */
            }
        });
    }

    // Método de salvamento FotoPerfilMotorista ( Activity FotoPerfilMotorista )
    public void salvarFotoPerfilMotorista(){

        // Salvar Imagem no FireBase cada child e uma pasta criada, no ultimo e nome da imagem
        StorageReference imagemRef = storageReference
                .child("motorista")
                .child(idUsuario)
                .child("documento de validacao")
                .child("Perfil")
                .child(idUsuario+".Perfil.JPEG");

        // Método para realmente salvar no Firebase
        // putBytes = para os dados da imagem em bytes
        UploadTask uploadTask = imagemRef.putBytes (fotoMotorista);
        // Método exibe uma mensagem caso ocorra uma falha na transferencia da imagem
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(CadastroCrlvMotoristaActivity.this,
                        "Erro ao fazer upload da imagem Foto de Pefil",
                        Toast.LENGTH_SHORT).show();

            }
            // método exibe uma mensagem caso tenha sucesso ao enviar
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }

    // Método de salvemento FotoDccumentoVeiculo ( Activity EnviarFotoDocumentoVeiculo )
    public void salvarFotoCrlvMotorista(){

        // Salvar Imagem no FireBase cada child e uma pasta criada, no ultimo e nome da imagem
        StorageReference imagemRef = storageReference
                .child("motorista")
                .child(idUsuario)
                .child("documento de validacao")
                .child("CRVL")
                .child(idUsuario+".CRVL.JPEG");

        // Método para realmente salvar no Firebase
        // putBytes = para os dados da imagem em bytes
        UploadTask uploadTask = imagemRef.putBytes (fotoCrlvMotorista);
        // Método exibe uma mensagem caso ocorra uma falha na transferencia da imagem
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(CadastroCrlvMotoristaActivity.this,
                        "Erro ao fazer upload da imagem CRLV",
                        Toast.LENGTH_SHORT).show();

            }
            // método exibe uma mensagem caso tenha sucesso ao enviar
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }
}