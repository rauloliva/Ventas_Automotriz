package com.automotriz.Presentacion;

import com.automotriz.VO.AutoVO;
import com.automotriz.VO.UsuarioVO;
import com.automotriz.logger.Logger;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import static com.automotriz.Constantes.Global.global;
import com.automotriz.Constantes.Constants;
import java.io.File;

public class Frame_AutoInfo extends javax.swing.JDialog implements Runnable, Constants<Frame_AutoInfo> {
    
    private final AutoVO auto;
    private List<String> imgs;
    private int count_imgs = 0;
    private Thread hiloSend;
    
    public Frame_AutoInfo(java.awt.Frame parent, boolean modal, AutoVO auto) {
        super(parent, modal);
        this.auto = auto;
        initComponents();
        initFrame(this);
    }
    
    @Override
    public void run() {
        //this part pf the thread is for sending the email
        try {
            hiloSend.sleep(5);
            sendNotification();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            Logger.error(e.getStackTrace());
        }
    }
    
    @Override
    public void initFrame(Frame_AutoInfo c) {
        String name = ReadProperties.props.getProperty("name.infoAuto");
        c.setName(name);
        c.setTitle(name);
        lbl_title.setText(name);
        Logger.log("Starting " + c.getName() + " frame...");
        Constants.metohds.setCloseIcon(lbl_close, this);
        //set the selected auto information int the form
        setAutoInfo();
        getDatosVendedor();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void getDatosVendedor() {
        try {
            int id_vendedor = auto.getId_usuario();
            Validacion validacion = new Validacion(new Object[]{id_vendedor});
            UsuarioVO vendedor = validacion.getVendedor();
            lbl_tel_vendedor.setText("Telefono: " + vendedor.getTelefono());
            lbl_correo_vendedor.setText("Correo: " + vendedor.getCorreo());
            //setting email and tel icons
            lbl_tel_icon.setIcon(new ImageIcon(
                    new ImageIcon(getClass().getResource(ReadProperties.props.getProperty("icon.telefono")))
                            .getImage()
                            .getScaledInstance(lbl_tel_icon.getWidth(), lbl_tel_icon.getHeight(), Image.SCALE_DEFAULT))
            );
            lbl_email_icon.setIcon(new ImageIcon(
                    new ImageIcon(getClass().getResource(ReadProperties.props.getProperty("icon.email")))
                            .getImage()
                            .getScaledInstance(lbl_email_icon.getWidth(), lbl_email_icon.getHeight(), Image.SCALE_DEFAULT))
            );
        } catch (Exception e) {
            Logger.error(e.getMessage());
            Logger.error(e.getStackTrace());
        }
    }
    
    private void setAutoInfo() {
        lbl_marca.setText("Marca: \t" + auto.getMarca());
        lbl_modelo.setText("Modelo: \t" + auto.getModelo());
        lbl_precio.setText("Precio: \t" + auto.getPrecio());
        lbl_km.setText("Kilometros: \t" + auto.getKilometros());
        lbl_cambio.setText("Cambio: \t" + auto.getCambio());
        lbl_color.setText("Color: \t" + auto.getColor());
        txa_descripcion.setText(auto.getDescripcion());
        setImages(0);
        if (!imgs.isEmpty()) {
            lbl_n_images.setText((count_imgs + 1) + "/" + imgs.size());
        } else {
            lbl_n_images.setText("0/0");
        }

        //validate if theres more than 1 image
        String siguiente_status = "ACTIVE", siguiente_icon = "icon.siguiente.on";
        if (hasNext()) {
            siguiente_status = "DISABLED";
            siguiente_icon = "icon.siguiente.off";
        }
        
        setIcon(ReadProperties.props.getProperty("icon.atras.off"), lbl_atras);
        ((ImageIcon) lbl_atras.getIcon()).setDescription("DISABLED");
        setIcon(ReadProperties.props.getProperty(siguiente_icon), lbl_siguiente);
        ((ImageIcon) lbl_siguiente.getIcon()).setDescription(siguiente_status);
    }
    
    private void setIcon(String imagePath, JLabel lbl) {
        lbl.setIcon(null);
        Image image = new ImageIcon(getClass().getResource(imagePath)).getImage();
        image = image.getScaledInstance(57, 47, Image.SCALE_DEFAULT);
        lbl.setIcon(new ImageIcon(image));
    }
    
    private void setImages(int i) {
        ImageIcon image;
        if (!auto.getImagenes().equals("")) {
            imgs = Arrays.asList(auto.getImagenes().split(";"));
            image = new ImageIcon(imgs.get(i));
        } else {
            imgs = new ArrayList();
            image = new ImageIcon(getClass().getResource(ReadProperties.props.getProperty("icon.noImage")));
        }
        Image img = image.getImage();
        img = img.getScaledInstance(299, 242, Image.SCALE_DEFAULT);
        lbl_imagenes.setIcon(new ImageIcon(img));
    }
    
    private boolean hasNext() {
        return count_imgs + 1 == imgs.size();
    }
    
    private void atrasAction() {
        setImages(--count_imgs);
        String atras_status = "ACTIVE", atras_icon = "icon.atras.on";
        if (count_imgs == 0) {
            atras_status = "DISABLED";
            atras_icon = "icon.atras.off";
        }
        //update the label
        lbl_n_images.setText((count_imgs + 1) + "/" + imgs.size());
        
        setIcon(ReadProperties.props.getProperty(atras_icon), lbl_atras);
        ((ImageIcon) lbl_atras.getIcon()).setDescription(atras_status);
        setIcon(ReadProperties.props.getProperty("icon.siguiente.on"), lbl_siguiente);
        ((ImageIcon) lbl_siguiente.getIcon()).setDescription("ACTIVE");
    }
    
    private void siguienteAction() {
        setImages(++count_imgs);
        String siguiente_status = "ACTIVE", siguiente_icon = "icon.siguiente.on";
        if (count_imgs == imgs.size() - 1) {
            siguiente_status = "DISABLED";
            siguiente_icon = "icon.siguiente.off";
        }
        //update the label
        lbl_n_images.setText((count_imgs + 1) + "/" + imgs.size());
        
        setIcon(ReadProperties.props.getProperty("icon.atras.on"), lbl_atras);
        ((ImageIcon) lbl_atras.getIcon()).setDescription("ACTIVE");
        setIcon(ReadProperties.props.getProperty(siguiente_icon), lbl_siguiente);
        ((ImageIcon) lbl_siguiente.getIcon()).setDescription(siguiente_status);
    }
    
    private void goToMail() {
        int option = JOptionPane.showOptionDialog(this,
                ReadProperties.props.getProperty("msg.redireccion.correo"),
                ReadProperties.props.getProperty("msg.redireccion.correo.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"CONTINUAR", "NO"}, "NO");
        
        if (option == JOptionPane.YES_OPTION) {
            for (JInternalFrame frame : global.getContainer().getAllFrames()) {
                frame.dispose();
            }
            global.getContainer().removeAll();
            this.dispose();
            global.getContainer().add(new Frame_EnviarCorreo(lbl_correo_vendedor.getText().replace("Correo:", "").trim()));
        }
    }
    
    private void sendNotification() {
        lbl_set_favorite.setBorder(new BevelBorder(BevelBorder.LOWERED));
        Validacion validacion = new Validacion(new Object[]{
            lbl_correo_vendedor.getText().replace("Correo:", "").trim(),
            ReadProperties.props.getProperty("mail.default.asunto"),
            ReadProperties.props.getProperty("mail.default.body")
            /*Replacing with the session's information*/
            .replace("[Nombre]", global.getSession().getNombre())
            .replace("[Correo]", global.getSession().getMail())
            .replace("[Telefono]", global.getSession().getTelefono()),
            /*Sending an image of the desire vehicle*/
            new File(auto.getImagenes().split(";")[0]),
            auto.getId()
        });
        validacion.sendMailToVendedor();
        lbl_set_favorite.setBorder(new BevelBorder(BevelBorder.RAISED));
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelContent = new javax.swing.JPanel();
        lbl_marca = new javax.swing.JLabel();
        lbl_modelo = new javax.swing.JLabel();
        lbl_precio = new javax.swing.JLabel();
        lbl_km = new javax.swing.JLabel();
        lbl_cambio = new javax.swing.JLabel();
        lbl_color = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txa_descripcion = new javax.swing.JTextArea();
        lbl_imagenes = new javax.swing.JLabel();
        lbl_atras = new javax.swing.JLabel();
        lbl_siguiente = new javax.swing.JLabel();
        lbl_n_images = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lbl_tel_vendedor = new javax.swing.JLabel();
        lbl_correo_vendedor = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lbl_tel_icon = new javax.swing.JLabel();
        lbl_email_icon = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        panelFavorito = new javax.swing.JPanel();
        lbl_set_favorite = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lbl_close = new javax.swing.JLabel();
        lbl_title = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);

        panelContent.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        lbl_marca.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        lbl_marca.setText("Marca");
        lbl_marca.setToolTipText("");

        lbl_modelo.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        lbl_modelo.setText("Modelo");

        lbl_precio.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        lbl_precio.setText("Precio");

        lbl_km.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        lbl_km.setText("Kilometros");

        lbl_cambio.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        lbl_cambio.setText("Cambio");

        lbl_color.setFont(new java.awt.Font("Tahoma", 0, 17)); // NOI18N
        lbl_color.setText("Color");

        txa_descripcion.setEditable(false);
        txa_descripcion.setColumns(20);
        txa_descripcion.setFont(new java.awt.Font("Segoe UI", 0, 17)); // NOI18N
        txa_descripcion.setRows(5);
        jScrollPane1.setViewportView(txa_descripcion);

        lbl_imagenes.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lbl_atras.setToolTipText("Imagen Anterior");
        lbl_atras.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lbl_atras.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_atras.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_atrasMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_atrasMouseExited(evt);
            }
        });

        lbl_siguiente.setToolTipText("Siguiente Imagen");
        lbl_siguiente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lbl_siguiente.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_siguiente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_siguienteMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_siguienteMouseExited(evt);
            }
        });

        lbl_n_images.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        lbl_tel_vendedor.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N

        lbl_correo_vendedor.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lbl_correo_vendedor.setText(":");
        lbl_correo_vendedor.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_correo_vendedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lbl_correo_vendedorMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_correo_vendedorMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lbl_correo_vendedorMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lbl_correo_vendedorMouseReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 20)); // NOI18N
        jLabel1.setText("Datos de contacto");

        lbl_tel_icon.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lbl_email_icon.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        jLabel2.setText("Seleccione el correo para enviar un mensaje");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lbl_tel_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lbl_tel_vendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lbl_email_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lbl_correo_vendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(jLabel2))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_tel_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_email_icon, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(lbl_tel_vendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(lbl_correo_vendedor)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        panelFavorito.setBackground(new java.awt.Color(255, 51, 0));
        panelFavorito.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        lbl_set_favorite.setBackground(new java.awt.Color(255, 51, 51));
        lbl_set_favorite.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        lbl_set_favorite.setForeground(new java.awt.Color(255, 255, 255));
        lbl_set_favorite.setText("  Lo Quiero!");
        lbl_set_favorite.setToolTipText("Agregar a Lista de Deseos");
        lbl_set_favorite.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        lbl_set_favorite.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_set_favorite.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_set_favoriteMouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lbl_set_favoriteMouseExited(evt);
            }
        });

        javax.swing.GroupLayout panelFavoritoLayout = new javax.swing.GroupLayout(panelFavorito);
        panelFavorito.setLayout(panelFavoritoLayout);
        panelFavoritoLayout.setHorizontalGroup(
            panelFavoritoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFavoritoLayout.createSequentialGroup()
                .addComponent(lbl_set_favorite, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelFavoritoLayout.setVerticalGroup(
            panelFavoritoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbl_set_favorite, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 19)); // NOI18N
        jLabel3.setText("Hazle saber al vendedor tu interes en su vehiculo");

        javax.swing.GroupLayout panelContentLayout = new javax.swing.GroupLayout(panelContent);
        panelContent.setLayout(panelContentLayout);
        panelContentLayout.setHorizontalGroup(
            panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelContentLayout.createSequentialGroup()
                        .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelContentLayout.createSequentialGroup()
                                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelContentLayout.createSequentialGroup()
                                        .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(lbl_marca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(lbl_modelo, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE))
                                            .addComponent(jScrollPane1))
                                        .addGap(39, 39, 39))
                                    .addGroup(panelContentLayout.createSequentialGroup()
                                        .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lbl_precio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lbl_km, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                                            .addComponent(lbl_cambio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(lbl_color, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbl_imagenes, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(panelContentLayout.createSequentialGroup()
                                        .addComponent(lbl_atras, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(72, 72, 72)
                                        .addComponent(lbl_n_images, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(60, 60, 60)
                                        .addComponent(lbl_siguiente, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(22, 22, 22))
                    .addGroup(panelContentLayout.createSequentialGroup()
                        .addComponent(panelFavorito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel3)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        panelContentLayout.setVerticalGroup(
            panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelContentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelContentLayout.createSequentialGroup()
                        .addComponent(lbl_marca)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbl_modelo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_precio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbl_km)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbl_cambio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbl_color)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelContentLayout.createSequentialGroup()
                        .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lbl_siguiente, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbl_atras, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lbl_n_images, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbl_imagenes, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(panelContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelContentLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(panelFavorito, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18))
                    .addGroup(panelContentLayout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)))
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(0, 51, 51));

        lbl_close.setToolTipText("Cerrar la Aplicacion");
        lbl_close.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lbl_close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_closeMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lbl_closeMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                lbl_closeMouseReleased(evt);
            }
        });

        lbl_title.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lbl_title.setForeground(new java.awt.Color(255, 255, 255));
        lbl_title.setText("jLabel3");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbl_close, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(39, 39, 39))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lbl_close, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_title)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lbl_atrasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_atrasMouseClicked
        if (((ImageIcon) lbl_atras.getIcon()).getDescription().equals("ACTIVE")) {
            lbl_atras.setBorder(new BevelBorder(BevelBorder.LOWERED));
            atrasAction();
        }
    }//GEN-LAST:event_lbl_atrasMouseClicked

    private void lbl_siguienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_siguienteMouseClicked
        if (((ImageIcon) lbl_siguiente.getIcon()).getDescription().equals("ACTIVE")) {
            lbl_siguiente.setBorder(new BevelBorder(BevelBorder.LOWERED));
            siguienteAction();
        }
    }//GEN-LAST:event_lbl_siguienteMouseClicked

    private void lbl_atrasMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_atrasMouseExited
        if (((ImageIcon) lbl_atras.getIcon()).getDescription().equals("ACTIVE")) {
            lbl_atras.setBorder(new BevelBorder(BevelBorder.RAISED));
        }
    }//GEN-LAST:event_lbl_atrasMouseExited

    private void lbl_siguienteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_siguienteMouseExited
        if (((ImageIcon) lbl_siguiente.getIcon()).getDescription().equals("ACTIVE")) {
            lbl_siguiente.setBorder(new BevelBorder(BevelBorder.RAISED));
        }
    }//GEN-LAST:event_lbl_siguienteMouseExited

    private void lbl_correo_vendedorMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_correo_vendedorMouseEntered
        lbl_correo_vendedor.setForeground(Color.decode(ReadProperties.props.getProperty("color.blue")));
        lbl_correo_vendedor.setFont(new Font("Tahoma", Font.PLAIN, 20));
    }//GEN-LAST:event_lbl_correo_vendedorMouseEntered

    private void lbl_correo_vendedorMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_correo_vendedorMouseExited
        lbl_correo_vendedor.setForeground(Color.decode(ReadProperties.props.getProperty("color.black")));
        lbl_correo_vendedor.setFont(new Font("Tahoma", Font.PLAIN, 18));
    }//GEN-LAST:event_lbl_correo_vendedorMouseExited

    private void lbl_correo_vendedorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_correo_vendedorMousePressed
        lbl_correo_vendedor.setForeground(Color.decode(ReadProperties.props.getProperty("color.red")));
    }//GEN-LAST:event_lbl_correo_vendedorMousePressed

    private void lbl_correo_vendedorMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_correo_vendedorMouseReleased
        lbl_correo_vendedor.setForeground(Color.decode(ReadProperties.props.getProperty("color.blue")));
        goToMail();
    }//GEN-LAST:event_lbl_correo_vendedorMouseReleased

    private void lbl_closeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_closeMouseClicked
        this.dispose();
    }//GEN-LAST:event_lbl_closeMouseClicked

    private void lbl_closeMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_closeMousePressed
        lbl_close.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }//GEN-LAST:event_lbl_closeMousePressed

    private void lbl_closeMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_closeMouseReleased
        lbl_close.setBorder(null);
    }//GEN-LAST:event_lbl_closeMouseReleased

    private void lbl_set_favoriteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_set_favoriteMouseClicked
        lbl_set_favorite.setBorder(new BevelBorder(BevelBorder.LOWERED));
        hiloSend = new Thread(this);
        hiloSend.start();
    }//GEN-LAST:event_lbl_set_favoriteMouseClicked

    private void lbl_set_favoriteMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_set_favoriteMouseExited
        lbl_set_favorite.setBorder(new BevelBorder(BevelBorder.RAISED));
    }//GEN-LAST:event_lbl_set_favoriteMouseExited

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_atras;
    private javax.swing.JLabel lbl_cambio;
    private javax.swing.JLabel lbl_close;
    private javax.swing.JLabel lbl_color;
    private javax.swing.JLabel lbl_correo_vendedor;
    private javax.swing.JLabel lbl_email_icon;
    private javax.swing.JLabel lbl_imagenes;
    private javax.swing.JLabel lbl_km;
    private javax.swing.JLabel lbl_marca;
    private javax.swing.JLabel lbl_modelo;
    private javax.swing.JLabel lbl_n_images;
    private javax.swing.JLabel lbl_precio;
    private javax.swing.JLabel lbl_set_favorite;
    private javax.swing.JLabel lbl_siguiente;
    private javax.swing.JLabel lbl_tel_icon;
    private javax.swing.JLabel lbl_tel_vendedor;
    private javax.swing.JLabel lbl_title;
    private javax.swing.JPanel panelContent;
    private javax.swing.JPanel panelFavorito;
    private javax.swing.JTextArea txa_descripcion;
    // End of variables declaration//GEN-END:variables
}
