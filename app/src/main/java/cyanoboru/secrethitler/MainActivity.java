package cyanoboru.secrethitler;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cyanoboru.secrethitler.core.CartaDeLey;
import cyanoboru.secrethitler.core.Hitler;
import cyanoboru.secrethitler.core.Jugador;
import cyanoboru.secrethitler.core.Partida;
import cyanoboru.secrethitler.core.Tablero;

public class MainActivity extends AppCompatActivity {

    protected static int REQUEST_CARTADELEY = 1;
    protected static int REQUEST_JUGADORES = 2;
    protected static int REQUEST_CARTADELEY_FAIL = 3;
    protected static int REQUEST_ASESINATO = 4;
    protected static int REQUEST_ASESINATO_HITLER = 5;

    protected Partida partida;
    protected Button leg;
    protected Button caos;
    private Button BotonGameOver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        partida = Partida.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BotonGameOver = (Button) findViewById(R.id.BotonGameOver);
        BotonGameOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToVictory("Los fascistas han ganado");
            }
        });
        BotonGameOver.setVisibility(View.GONE);

        leg = (Button) findViewById(R.id.legislacionButton);
        leg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Lanzamiento de una legislacion
                startActivityForResult(new Intent(MainActivity.this, legislar.class), REQUEST_CARTADELEY);
            }
        });

        caos = (Button) findViewById(R.id.CaosButton);
        caos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //aumenta caos
                CartaDeLey aux = partida.incrementarCaos();
                if(aux != null);
                //LANZAR METODO QUE ACTUALICE LOS CAMPOS NECESARIOS
                actualizarTodo();
                if(partida.getTablero().getCaos()==0){
                    Toast.makeText(MainActivity.this,"LEY APROBADA POR CAOS", Toast.LENGTH_LONG).show();
                    if(aux.getLey().equals("Fascista")){
                        checkPoderes();
                    }
                }
            }
        });

        if (!partida.rolesListos){
            startActivityForResult(new Intent(MainActivity.this, add_player.class), REQUEST_JUGADORES);
            // redirect main activity to add player
        }
        if (partida.rolesListos){
            actualizarTodo();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_CARTADELEY) {
            if (resultCode == REQUEST_CARTADELEY_FAIL) {
                Toast.makeText(MainActivity.this, "DERECHO A VETO", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "LEY APROBADA POR LEGISLACION", Toast.LENGTH_LONG).show();
                Partida.getInstance().getTablero().aprobarLey((CartaDeLey) data.getExtras().getSerializable("carta"));
                actualizarTodo();
                if (((CartaDeLey) data.getExtras().getSerializable("carta")).getLey().equals("Fascista")) {
                    checkPoderes();
                }
            }
        }else{
            if(requestCode == REQUEST_ASESINATO && resultCode == REQUEST_ASESINATO_HITLER){
                sendToVictory("Los liberales ganan");
            }else {
                actualizarTodo();
            }
        }
    }

    //Actualiza todos los textview necesarios para mostrar que esta pasando en el tablero
    public void actualizarTodo(){
        vitoria();
        TextView update;
        int fascistasAprobadas = partida.getTablero().getFascistas();
        //Acualizamos el contador de liberales y fascistas
        update = (TextView) findViewById(R.id.FascistasAprobadas);
        update.setText(String.valueOf(fascistasAprobadas));
        update = (TextView) findViewById(R.id.LiberalesAprobadas);
        update.setText(String.valueOf(partida.getTablero().getLiberales()));

        //Actualizamos el contador de caos
        update = (TextView) findViewById(R.id.NivelDeCaos);
        update.setText(String.valueOf(partida.getTablero().getCaos()));

        //Actualizamos el textbox de info importante
        TextView extraText = (TextView) findViewById(R.id.ExtraText);

        String toShow = ""; //reset del string

        //Los dos casos ininmutables
        toShow+= "El presidente nombra y todos votais al siguiente canciller";
        if(fascistasAprobadas>=3){
            toShow +=  "\nSi el próximo canciller es Hitler, ganaran esos cerdos fascistas";
            BotonGameOver.setVisibility(View.VISIBLE);
        }

        //Texto variante en funcion del numero de jugadores
        if(partida.getJugadores().size()<7) { //---5 o 6---
            if(fascistasAprobadas==2){
                toShow += "\nLa proxima ley fascista otorga el poder de ver las siguientes 3 cartas";
            }
            if(fascistasAprobadas>2){
                toShow += "\nLa proxima ley fascista otroga el poder del asesinato";
            }
        }else{
            if(partida.getJugadores().size()<9) { //---7 o 8---
                if(fascistasAprobadas==1){
                    toShow += "\nLa proxima ley fascista otorga el poder de investigar la afiliacion politica de un jugador";
                }
                if(fascistasAprobadas==2){
                    toShow += "\nLa proxima ley fascista otroga el poder de elegir el proximo presidente a dedo";
                }
                if(fascistasAprobadas>2){
                    toShow += "\nLa proxima ley fascista otroga el poder del asesinato";
                }
            }else{ //---9 o 10---
                if(fascistasAprobadas==0){
                    toShow += "\nLa proxima ley fascista otorga el poder de investigar la afiliacion politica de un jugador";
                }
                if(fascistasAprobadas==1){
                    toShow += "\nLa proxima ley fascista otorga el poder de investigar la afiliacion politica de un jugador";
                }
                if(fascistasAprobadas==2){
                    toShow += "\nLa proxima ley fascista otroga el poder de elegir el proximo presidente a dedo";
                }
                if(fascistasAprobadas>2){
                    toShow += "\nLa proxima ley fascista otroga el poder del asesinato";
                }
            }
        }
        if(fascistasAprobadas==5) {
            toShow += "\nA partir de ahora, el canciller puede negarse a aprobar las leyes que le lleguen";
        }
        extraText.setText(toShow);
    }

    public void checkPoderes(){
        vitoria();
        int fascistasAprobadas = partida.getTablero().getFascistas();
        int numJugadores = partida.getJugadores().size();
        if(fascistasAprobadas == 1 && numJugadores > 8){
            startActivity(new Intent(MainActivity.this, InvestigarJugador.class));
        }
        if(fascistasAprobadas == 2 && numJugadores > 6){
            startActivity(new Intent(MainActivity.this, InvestigarJugador.class));
        }
        if(fascistasAprobadas == 3){
            if(numJugadores<7){
                startActivity(new Intent(MainActivity.this, ver3cartas.class));
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
                builder.setTitle( "Presidente designado" );
                builder.setMessage( "El presidente actual elige al próximo presidente.\n" +
                        "Nadie puede oponerse a esta decisión, y tras su mandato el orden normal se retoma" );
                builder.setPositiveButton("Ok!", null);
                builder.create().show();
                //Presidente a dedo
            }
        }
        if(fascistasAprobadas > 3 && fascistasAprobadas<6){
            startActivityForResult(new Intent(MainActivity.this, MatarJugador.class),REQUEST_ASESINATO);
        }

    }

    public void vitoria(){
        Log.d("Comprobando victoria","d");
        if(partida.getTablero().getLiberales() >= 5){
            sendToVictory("Los liberales ganan");
        }else if(partida.getTablero().getFascistas() >= 6){
            sendToVictory("Los fascistas han ganado");
        }else if(!hitlerEstaVivo()){
            sendToVictory("Los liberales ganan");
        }
    }

    public void sendToVictory(String t){
        Intent i = new Intent(this, vitoria.class);
        i.putExtra("winners",t);
        startActivity(i);
    }

    public boolean hitlerEstaVivo(){
        for(Jugador j: partida.getJugadores()){
            if(j.getCartaDeIdentidad().getClass() == Hitler.class){ // ouch
                return j.estaVivo();
            }
        }
        return false;
    }

    // menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        this.getMenuInflater().inflate(R.menu.menuppal,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        if(menuItem.getItemId() == R.id.menuReiniciar){
            reiniciarPartida();
        }
        return true;
    }

    public void reiniciarPartida(){
        Partida.clear();
        startActivityForResult(new Intent(MainActivity.this, add_player.class), REQUEST_JUGADORES);
    }
}