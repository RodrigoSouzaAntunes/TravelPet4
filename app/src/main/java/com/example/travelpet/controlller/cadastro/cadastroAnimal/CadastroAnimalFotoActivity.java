package com.example.travelpet.controlller.cadastro.cadastroAnimal;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelpet.R;
import com.example.travelpet.model.Animal;
import com.example.travelpet.model.Usuario;
import com.example.travelpet.dao.ConfiguracaoFirebase;
import com.example.travelpet.dao.UsuarioFirebase;
import com.example.travelpet.helper.Permissao;
import com.example.travelpet.controlller.perfil.passageiro.PerfilPassageiroActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroAnimalFotoActivity extends AppCompatActivity {

    // Variaveis usadas para armazenar dados da Activity CadastroAnimalPorte
    private String nomeUsuario, sobrenomeUsuario, telefoneUsuario,tipoUsuario,fotoUsuarioUrl,
            nomeAnimal, especieAnimal, racaAnimal, porteAnimal, observacaoAnimal, fotoAnimalUrl,idAnimal;

    private String fluxoDados;
    private String localSalvamentoAnimal;

    private CircleImageView imageViewFotoAnimal;

    // Variáveis usadas para especificar o requestCode
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;

    private StorageReference storageReference;
    private String emailUsuario;

    private byte[] fotoAnimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_animal_foto);

        overridePendingTransition(R.anim.activity_filho_entrando, R.anim.activity_pai_saindo);

        Intent intent = getIntent();
        Usuario usuario = intent.getParcelableExtra("usuario");
        Animal animal = intent.getParcelableExtra("animal");

        // Dados da classe Usuario
        nomeUsuario         =   usuario.getNome();
        sobrenomeUsuario    =   usuario.getSobrenome();
        telefoneUsuario     =   usuario.getTelefone();
        tipoUsuario         =   usuario.getTipoUsuario();
        fluxoDados          =   usuario.getFluxoDados();

        // Dados da classe Animal
        idAnimal            =   Animal.gerarPushKeyIdAnimal();
        nomeAnimal          =   animal.getNomeAnimal();
        especieAnimal       =   animal.getEspecieAnimal();
        racaAnimal          =   animal.getRacaAnimal();
        porteAnimal         =   animal.getPorteAnimal();
        observacaoAnimal    =   animal.getObservacaoAnimal();

        // Variável usada no processo de pegar foto Url do usuario firebase
        FirebaseUser user = UsuarioFirebase.getUsuarioAtual();
        // Se o Uusário não tiver foto na conta gmail então
        if(fotoUsuarioUrl == null){
            // envia "vazio" para o database
            fotoUsuarioUrl = "vazio";
        }else {// se não vai enviar a foto do Usuario do Firebase, que já esta no gmail
            fotoUsuarioUrl = user.getPhotoUrl().toString();
        }
        // Recupera a referência do Storage
        storageReference    =   ConfiguracaoFirebase.getFirebaseStorage();
        emailUsuario        =   UsuarioFirebase.getEmailUsuario();

        imageViewFotoAnimal = findViewById(R.id.imageViewFotoAnimal);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                Permissao.alertaValidacaoPermissao(CadastroAnimalFotoActivity.this);
            }
        }
    }
    public void buttonCamera(View view){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Verifica se a intent conseguiu fazer o pedido ( que e abrir a camera)
        // getActivity(). = foi necessário colocar pois estamos dentro de um Fragment
        // e não uma Activity
        // getPackageManager() retorna uma classe um objeto packageManager responsavel
        // por obter várias informações relacionado a aplicação
        if (i.resolveActivity(getPackageManager()) != null) {
            // Captura a foto que foi tirada
            // startActivityForResult = Inicia a activity, e recupera um resultado de retorno que e a foto
            // requestCode= para saber qual ação foi executada a da camera ou da galeria
            //startActivityForResult(i, SELECAO_CAMERA );
            startActivityForResult(i,SELECAO_CAMERA);
        }
    }

    public void buttonGaleria(View view){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i,SELECAO_GALERIA);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Teste resultCode
        // se for igual ao RESULT_OK, deu td ok
        if( resultCode == RESULT_OK){
            // null = por que pode receber dados de dois lugares , camera ou galeria
            Bitmap imagem = null;

            try{

                switch (requestCode){
                    case SELECAO_CAMERA:
                        // data = parâmetro refênciado la encima no "onActivityResult"
                        // getExtras() = recupera recursos extras
                        // get("data"); = recebe uma String ("data) que e o dado retornado
                        // (Bitmap) = e um cash para Bitmap, referenciando que e o tipo da variável
                        imagem = (Bitmap) data.getExtras().get("data");

                        break;
                    case SELECAO_GALERIA:
                        // Recupera o local da imagem selecionada
                        // data.getData(); = local da imagem
                        Uri localImagemSelecionada = data.getData();
                        // getContentResolver() = responsável por recupera conteúdo dentro do app android
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);
                        break;
                }
                // Verificando se a imagem não está vazia
                if(imagem != null){
                    // Envia a imagem da camera ou galeria para o imagemView do xml
                    imageViewFotoAnimal.setImageBitmap( imagem );

                    // Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress( Bitmap.CompressFormat.JPEG, 100, baos );
                    fotoAnimal = baos.toByteArray();

                    Toast.makeText(CadastroAnimalFotoActivity.this,
                            "Sucesso ao selececionar a imagem",
                            Toast.LENGTH_SHORT).show();

                }
            }catch (Exception e){
                // Caso de algum erro, e possivvel visualizar no "e.printStackTrace();"
                e.printStackTrace();
            }
        }
    }

    public void buttonFinalizarCadastroPassageiro(View view) {

        if (fotoAnimal != null && fluxoDados.equals("cadastroUsuario")) {
            String localSalvamentoUsuario = "CadastroAnimalFotoActivity";
            Usuario usuario = new Usuario();

            usuario.setId(UsuarioFirebase.getIdentificadorUsuario());
            usuario.setEmail(UsuarioFirebase.getEmailUsuario());
            usuario.setNome(nomeUsuario);
            usuario.setSobrenome(sobrenomeUsuario);
            usuario.setTelefone(telefoneUsuario);
            usuario.setTipoUsuario(tipoUsuario);
            usuario.setFotoUsuarioUrl(fotoUsuarioUrl);
            usuario.salvar(CadastroAnimalFotoActivity.this, localSalvamentoUsuario);
            localSalvamentoAnimal = "CadastroAnimalFotoActivity_cadastroUsuario";
            salvarFotoAnimal(localSalvamentoAnimal);

            Intent intent = new Intent(CadastroAnimalFotoActivity.this, PerfilPassageiroActivity.class);
            // intent.setFlags = Limpa as activitys acumuladas
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);


        }else if(fotoAnimal != null && fluxoDados.equals("perfilUsuario")){
            localSalvamentoAnimal = "CadastroAnimalFotoActivity_adicionarAnimal";
            salvarFotoAnimal(localSalvamentoAnimal);

            Intent intent = new Intent(CadastroAnimalFotoActivity.this, PerfilPassageiroActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
        else{

            Toast.makeText(CadastroAnimalFotoActivity.this,
                    "Envie a foto do seu Animal ",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void salvarFotoAnimal(final String localSalvamentoAnimal){
        // Salvar imagem no firebase
        StorageReference imagemRef = storageReference
                .child("animais")
                .child(emailUsuario)
                .child(idAnimal)
                .child(idAnimal+".FOTO.PERFIL.JPEG");

        // Salvando dados da imagem método UploadTask
        // .putBytes = passa os dados da imagem em Bytes
        UploadTask uploadTask = imagemRef.putBytes(fotoAnimal);

        // Método para saber se o salvamento deu certo no Storage
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(CadastroAnimalFotoActivity.this,
                        "Erro ao fazer upload da imagem",
                        Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Método para pegar o caminho(url) da foto, quando salvar ela no storage
                Task<Uri> uri = taskSnapshot.getStorage().getDownloadUrl();
                while(!uri.isComplete());
                // url = pega o caminho da imagem
                Uri url = uri.getResult();
                // transforma a url para String, e armazena na variável
                fotoAnimalUrl = url.toString();

                //String localSalvamentoAnimal = localSalvamentoAnimal;
                // Método para salvar animal
                // foi feito aqui por causa do método que pega o caminho da url da foto
                Animal animal = new Animal();

                animal.setIdUsuario(UsuarioFirebase.getIdentificadorUsuario());
                animal.setIdAnimal(idAnimal);
                animal.setNomeAnimal(nomeAnimal);
                animal.setEspecieAnimal(especieAnimal);
                animal.setRacaAnimal(racaAnimal);
                animal.setPorteAnimal(porteAnimal);
                animal.setObservacaoAnimal(observacaoAnimal);
                animal.setFotoAnimal(fotoAnimalUrl);
                animal.salvarAnimal(CadastroAnimalFotoActivity.this, localSalvamentoAnimal);

            }
        });
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_pai_entrando, R.anim.activity_filho_saindo);
    }
}



