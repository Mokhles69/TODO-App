package com.aaup.mokhlesturkman.todo_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.HashMap;
import java.util.Map;

public class Tasks extends AppCompatActivity {


    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private EditText mTodo;
    FirebaseFirestore mDb;
    private static final String TAG = "TASKS";
    LinearLayout mTodoList;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        mAuth= FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mTodo = findViewById(R.id.editTextAdd);
        mDb = FirebaseFirestore.getInstance();
        mTodoList = findViewById(R.id.linearLayout);


        FloatingActionButton add = findViewById(R.id.add);
        FloatingActionButton logout = findViewById(R.id.logout);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addTodo(currentUser.getUid(), mTodo.getText().toString());
                mTodo.setText("");


            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        fetchTodos();

    }


    private void fetchTodos() {
        Log.d(TAG,"Fetch TODOS");
        mDb.collection("users")
                .whereEqualTo("name", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "fetchTodos failed", task.getException());
                            return;
                        }

                        for (QueryDocumentSnapshot todo : task.getResult()) {
                            String ttask = todo.get("todo", String.class);
                            insertTodoUi(ttask);
                        }
                    }
                });
    }


    private void insertTodoUi(String name) {
        // Add to View
        TextView todo = new TextView(getApplicationContext());
        todo.setTextSize(14);
        todo.setText(name);
        mTodoList.addView(todo);
    }

    private void addTodo(String userId, final String name) {

        // Add to Firebase
        Map<String, Object> todo = new HashMap<>();
        todo.put("name", userId);
        todo.put("todo", name);


        mDb.collection("users")
                .add(todo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        insertTodoUi(name);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "addTodoFailed", e);
                    }
                });
    }
}
