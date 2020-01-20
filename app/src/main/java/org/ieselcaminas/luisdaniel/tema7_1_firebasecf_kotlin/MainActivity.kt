package org.ieselcaminas.luisdaniel.tema7_1_firebasecf_kotlin




import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var listenerUltimMissatge: ListenerRegistration? = null
    var listenerMissatges: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        boto.text = "Enviar"
        val pantPrincipal = this

        // Referències a la Base de Dades i als documents
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        var docRef: DocumentReference = db.collection("Xats").document("XatProva");

        // Exemple de llegir tots els documents d'una col·lecció
        // Per a triar el xat
        db.collection("Xats").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val opcions = ArrayList<String>()
                for (document in task.result!!) {
                    opcions.add(document.id)
                }
                val adaptador =
                    ArrayAdapter(pantPrincipal, android.R.layout.simple_spinner_item, opcions)
                adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                comboXats.adapter = adaptador
            } else {
            }
        }

        comboXats.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {

            override fun onItemSelected(arg0:AdapterView<*>, arg1: View, arg2:Int, arg3:Long) {
                // TODO Auto-generated method stub
                docRef = db.collection("Xats").document(comboXats.selectedItem.toString())
                area.text = ""
                inicialitzar()
            }

            private fun inicialitzar() {
                // Exemple de lectura única: AddOnSuccessListener()
                // Per a posar el títol. Sobre /Xats/XatProva/nomXat
                docRef.get().addOnSuccessListener { documentSnapshot ->
                    val nomXat = documentSnapshot.getString("nomXat")
                    title = nomXat
                }

                // Exemple de listener de lectura contínua addSnapshotListener() sobre un document
                // Per a posar l'últim missatge registrat. Sobre /Xats/XatProva/ultimMissatge
                // Si estava en marxa, el parem abans de tornar-lo a llançar
                if (listenerUltimMissatge != null)
                    listenerUltimMissatge!!.remove()

                listenerUltimMissatge = docRef.addSnapshotListener { documentSnapshot, e -> ultim.text = "Last message: " + documentSnapshot!!.getString("ultimMissatge") }

                // Exemple de listener de lectura contínua addSnapshotListener() sobre una col·lecció
                // Per a posar tota la llista de missatges. Sobre /Xats/XatProva/missatges
                // Si estava en marxa, el parem abans de tornar-lo a llançar
                if (listenerMissatges != null)
                    listenerMissatges!!.remove()

                listenerMissatges = db.collection("Xats").document(comboXats.selectedItem.toString()).collection("missatges")
                    .orderBy("data").addSnapshotListener { snapshots, e ->
                        for (dc in snapshots!!.documentChanges) {
                            when (dc.type) {
                                DocumentChange.Type.ADDED -> {
                                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm")
                                    val dataFormatejada = sdf.format(Date(dc.document.getLong("data")!!))
                                    area.append(
                                        dc.document.getString("nom") + " (" + dataFormatejada + "): " + dc.document.getString("contingut") + "\n"
                                    )
                                }
                            }
                        }
                    }

                // Per a guardar dades
                // Primer sobre /Xats/XatProva/ultimUsuari i /Xats/XatProva/ultimMissatge
                // Després també com a documents en la col·lecció /Xats/XatProva/missatges
                boto.setOnClickListener {
                    val dades = HashMap<String, Any>()
                    dades["ultimUsuari"] = usuari.text.toString()
                    dades["ultimMissatge"] = text.text.toString()
                    docRef.update(dades)

                    val m = Missatge(usuari.text.toString(), (Date()).time, text.text.toString())
                    db.collection("Xats").document(comboXats.selectedItem.toString()).collection("missatges").add(m)

                    text.setText("")
                }

            }

            override fun onNothingSelected(arg0:AdapterView<*>) {
                // TODO Auto-generated method stub

            }
        }

    }
}



