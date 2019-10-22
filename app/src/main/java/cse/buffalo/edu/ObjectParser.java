package cse.buffalo.edu;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ObjectParser {
    File objFile = null;
    private final static String OBJ_VERTEX_TEXTURE = "vt";
    private final static String OBJ_VERTEX_NORMAL = "vn";
    private final static String OBJ_VERTEX = "v";
    private final static String OBJ_FACE = "f";
    public final int MTL_KD = 1;
    public float[] vertexs;
    public ArrayList<Float> vertex= new ArrayList<Float>();
    public ArrayList<Float> normals= new ArrayList<Float>();
    public ArrayList<Float> vertexTexture= new ArrayList<Float>();
    //For aligning
    public ArrayList<List<Float>> texturealign= new ArrayList<List<Float>>();
    public ArrayList<List<Float>> vertexalign= new ArrayList<List<Float>>();
    public ArrayList<List<Float>> normalalign= new ArrayList<List<Float>>();

    public ArrayList<Integer> indices= new ArrayList<Integer>();

    public int[] faces;
    public ObjectParser(String filename) throws FileNotFoundException, IOException {
        parseObjFile(filename);
    }
    private void parseObjFile(String objFilename) throws FileNotFoundException, IOException{
        int lineCount = 0;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        objFile = new File(objFilename);
        fileReader = new FileReader(objFile);
        bufferedReader = new BufferedReader(fileReader);
        Log.e("filename:","buffer reader created");
        String line = null;
        while (true) {
            line = bufferedReader.readLine();
            if (null == line) {
                break;
            }

            line = line.trim();

            if (line.length() == 0) {
                continue;
            }

            // NOTE: we don't check for the space after the char
            // because sometimes it's not there - most notably in the
            // grouupname, we seem to get a lot of times where we have
            // "g\n", i.e. setting the group name to blank (or
            // default?)
            if (line.startsWith("#")) // comment
            {
                continue;
            }else if (line.startsWith(OBJ_VERTEX_TEXTURE)) {
                processVertexTexture(line);
                //
            } else if (line.startsWith(OBJ_VERTEX_NORMAL)) {
                processVertexNormals(line);
            }
            else if (line.startsWith(OBJ_VERTEX)) {
                processVertex(line);
            } else if (line.startsWith(OBJ_FACE)) {
                processFace(line);
            }
            lineCount++;
        }
        bufferedReader.close();
        // Log.d("Loaded: ",  lineCount + " lines");
    }
    private void processVertex(String line) {
        // Log.d("current line: "," "+line);
        vertexs = StringUtils.parseFloatList(3, line, OBJ_VERTEX.length());
        List<Float> m= new ArrayList<>();
        m.add(vertexs[0]);
        m.add(vertexs[1]);
        m.add(vertexs[2]);
        vertexalign.add(m);
    }
    private void processFace(String line) {
        line = line.substring(OBJ_FACE.length()).trim();
        faces = StringUtils.parseListVerticeNTuples(line, 3);
        String parsedList = "";
        int loopi = 0;
        while (loopi < faces.length) {
            parsedList = parsedList + "( "+faces[loopi] + " / "+faces[loopi+1] + " / "+faces[loopi+2] + " ) ";
            indices.add(faces[loopi]-1);
            List<Float> m=vertexalign.get(faces[loopi]-1);
            vertex.add(m.get(0));
            vertex.add(m.get(1));
            vertex.add(m.get(2));
            List<Float> temp=texturealign.get(faces[loopi+1]-1);
            vertexTexture.add(temp.get(0));
            vertexTexture.add(temp.get(1));
           // List<Float> ns=normalalign.get(faces[loopi+2]-1);
           // normals.add(ns.get(0));
           // normals.add(ns.get(1));
          //  normals.add(ns.get(2));
            loopi+=3;
        }
    }
    private void processVertexTexture(String line) {
        float[] values = StringUtils.parseFloatList(2, line, OBJ_VERTEX_TEXTURE.length());
        List<Float> m= new ArrayList<>();
        m.add(values[0]);
        m.add(values[1]);
        texturealign.add(m);
    }
    private void processVertexNormals(String line){
        float[] normals= StringUtils.parseFloatList(3, line, OBJ_VERTEX_NORMAL.length());
        List<Float> m= new ArrayList<>();
        m.add(normals[0]);
        m.add(normals[1]);
        m.add(normals[2]);
        normalalign.add(m);
    }

    public ArrayList<Float> getVertexs(){
        return vertex;
    }
    public ArrayList<Float> getVertexTex(){
        return vertexTexture;
    }
    public ArrayList<Float> getNormals(){
        return normals;
    }
    public ArrayList<Integer> getFaces(){
        return indices;
    }
}
