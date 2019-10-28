package com.automotriz.VO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UsuarioVO implements Serializable {

    private int id;
    private String usuario;
    private String contraseña;
    private String correo;
    private String perfil;
    private String estatus;
    private String telefono;
    private static final int nFields = 5;

    public UsuarioVO(int id, String usuario, String contraseña, String correo, String perfil, String estatus, String telefono) {
        this.id = id;
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.correo = correo;
        this.perfil = perfil;
        this.estatus = estatus;
        this.telefono = telefono;
    }

    public UsuarioVO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return "UsuarioVO{" + "id=" + id + ", usuario=" + usuario + ", contrase\u00f1a=" + contraseña + ", correo=" + correo + ", perfil=" + perfil + ", estatus=" + estatus + ", telefono=" + telefono + '}';
    }

    /**
     * Generate a list of String Arrays from a list of Usuarios
     *
     * @param usuarios the list of UsuarioVO to convert
     * @return a list of a String matrix
     */
    public static List<String[][]> usuariosAsMatrix(List<UsuarioVO> usuarios) {
        String[][] usuariosMatrix = new String[usuarios.size()][nFields];

        for (int i = 0; i < usuarios.size(); i++) {
            usuariosMatrix[i][0] = usuarios.get(i).getUsuario();
            usuariosMatrix[i][1] = usuarios.get(i).getCorreo();
            usuariosMatrix[i][2] = usuarios.get(i).getPerfil();
            usuariosMatrix[i][3] = usuarios.get(i).getEstatus();
            usuariosMatrix[i][4] = usuarios.get(i).getTelefono();
        }
        return saveMatrixOnList(usuariosMatrix);
    }

    private static List<String[][]> saveMatrixOnList(String[][] usuarios) {
        List<String[][]> usuariosList = new ArrayList();
        usuariosList.add(usuarios);
        return usuariosList;
    }
}