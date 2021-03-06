package Interface;

import GameElements.*;
import Listas.NodoList;
import Loops.TimeAnimation;
import Others.BasicFunctions;
import javafx.animation.AnimationTimer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Controller {
	
		/*Atributes*/
		//game interface elements
	    @FXML private AnchorPane mapAnchorPane = new AnchorPane();
	    @FXML private ImageView batteryImageView = new ImageView();
	    @FXML private ProgressBar powerProgressBar = new ProgressBar();
	    @FXML private Text aliveText =  new Text(); 
	    @FXML private Text  slayedText =  new Text(); 
	    @FXML private Text  countText =  new Text();
	    @FXML private Label labelpesos= new Label();
	    //game backgrounds elements
	    public static BackGround background = new BackGround();
	    private int count;
	    private long timei,timef,timepeligroi,timepeligrof;
	    private Boolean presionado=false;
	    private NodoList<Bala> listabala= new NodoList<>();
        private AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
					timepeligrof = System.currentTimeMillis()-timepeligroi;
					moverbalas();
					marcarcaminoslabel();
                    verificarchoque();
                    if (timepeligrof>6000){
                    	bajarpeligrosidad();
                    	timepeligroi=System.currentTimeMillis();
					}
                    actualizarlabel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

	private void actualizarlabel() {
		labelpesos.setText("Pesos de los caminos:");
		NodoList<Ruta> caminos =background.caminos;
		for (int n=0; n<caminos.getLargo(); n++){
			Ruta tmp=caminos.get(n);
			labelpesos.setText(labelpesos.getText()+"\n"+tmp.getInicio()+"-"+tmp.getFin()+": "+(int)tmp.getPeso());
		}
	}

	public  void marcarcaminoslabel(){
		NodoList<Label> labels=background.labels;
		for (int cont=0;cont<labels.getLargo();cont++){
			Label label =labels.get(cont);
			Plane plane=background.getPlanes().get(cont);
			label.setLayoutX(plane.getrealx()+40);
			label.setLayoutY(plane.getrealy()+40);
			label.setText(plane.print(plane.getCamino(),(char)0));
			if (!mapAnchorPane.getChildren().contains(label)){
				mapAnchorPane.getChildren().add(label);
				System.out.println("añado label al map");
			}
		}
	}

	public void moverbalas(){
		int cont;
		for (cont=0;cont<listabala.getLargo();cont++){
			Bala bala=listabala.get(cont);
			bala.setY(bala.getY()-bala.getVelocidad());
			if (bala.getY()<-30){listabala.eliminar(bala);mapAnchorPane.getChildren().remove(bala);}
		}
	}


	private void verificarchoque(){
    	NodoList<Plane> aviones= background.getPlanes();
    	int i,j;
    	for (i=0; i<aviones.getLargo();i++) {
			for (j = 0; j < listabala.getLargo(); j++) {
				Plane avion=aviones.get(i);
				Bala bala=listabala.get(j);
				if (Math.abs(bala.getPosx()-avion.getrealx())<32 && Math.abs(bala.getY()-avion.getrealy())<32){
					try {
						try {
							char siguiente = avion.getCamino().get(avion.getOnNode());
							char inicial = avion.getCurrentZone().getid();
							System.out.println("muerto en"+inicial+","+siguiente);
							if (inicial==siguiente){}
							else {subirpeligrosidad(inicial,siguiente);
							}
						}catch (NullPointerException e){
							System.out.println("no tengo ruta aun");
						}
						Label label=background.labels.get(i);
						mapAnchorPane.getChildren().remove(label);
						background.labels.eliminar(label);
						mapAnchorPane.getChildren().remove(bala);
						aviones.eliminar(avion);
						listabala.eliminar(bala);
						avion.slaye(this.mapAnchorPane);
						background.setSlayed();
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	private void subirpeligrosidad(char i ,char f){
    	System.out.println("voy a subir la peligrosidad");
		NodoList<Ruta> caminos =background.caminos;
		for (int n=0; n<caminos.getLargo(); n++){
			Ruta tmp=caminos.get(n);
			if ((tmp.inicio==i && tmp.fin==f) || (tmp.inicio==f && tmp.fin==i)){
				tmp.setPeligro(100);
				background.cambiarpeso(i,f,tmp);
			}
		}


	}
    private void bajarpeligrosidad(){
		System.out.println("voy a bajar la peligrosidad");
    	NodoList<Ruta> caminos =background.caminos;
    	for (int i=0; i<caminos.getLargo(); i++){
    		Ruta camino =caminos.get(i);
    		camino.bajarpeligro();
    		background.cambiarpeso(camino.inicio,camino.fin,camino);
		}
	}
	/*initializer*/ 
    public void initialize() throws InterruptedException {
    	setGraphics();
//    	setMusic();
    	TankEvent(); 
    	drawAir();
    	gentrTask();
    	pintarlineas();
//    	toDo();
//    	toDo();
    	
	}
	public void pintarlineas(){
    	NodoList<Line> lineas=background.lineas;
    	for (int i=0; i<lineas.getLargo(); i++){
    		mapAnchorPane.getChildren().add(lineas.get(i));
		}
	}

    
    //music
    private void setMusic() {
    	BasicFunctions.music();
    }
    
    //mapa y labels
    private void setGraphics() {
    	background.setAnchorPane(mapAnchorPane);
    	background.setAliveText(aliveText);
    	background.setSlayedText(slayedText);
	}

    
	//draw zones
    public void drawAir() {
    	for(int i=0; i<background.getZones().getLargo(); i++) { 
    		Air zone = background.getZones().get(i);
    		final StackPane s = new StackPane();
    		final Rectangle r = new Rectangle();
	        final Text t = new Text ();
    		if (zone instanceof AirPort) {
    	        r.setFill(Color.RED);
    	        r.setStroke(Color.BLUE);
    		}else {
    	        r.setFill(Color.GREEN);
    	        r.setStroke(Color.ORANGE);
    		}
	        t.setText(String.valueOf(zone.getid()));
	        t.setStyle("-fx-text-fill: white; -fx-font-size: 25px;");
	        r.setWidth(40);
	        r.setHeight(40);
	        r.setArcWidth(50);
	        r.setArcHeight(50);
	        s.setLayoutX(background.getZones().get(i).getPosx());
	        s.setLayoutY(background.getZones().get(i).getPosy());
	        s.getChildren().addAll(r, t);
	        mapAnchorPane.getChildren().add(s);
    	}
    }
   
    
    
    //tank listener-movement
    public void TankEvent() {
        batteryImageView.setFocusTraversable(true);
        batteryImageView.requestFocus();
        timer.start();
		timepeligroi = System.currentTimeMillis();
    	batteryImageView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE){
                if (!presionado) {
//                    System.out.println("he clickeado");
                    timei = System.currentTimeMillis();
                    progressTask();
//        			System.out.println(timei);
                    presionado=true;
                }
            }
        });
        batteryImageView.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SPACE){
                ImageView img=(ImageView) (event.getSource());
//                System.out.println("quite click");
                if (presionado) {
                    timef = System.currentTimeMillis()-timei;
                    Double velocidad= (Double.parseDouble(String.valueOf(timef))/1000+1)*3;
//                    System.out.println(img.getTranslateX());
                    Bala bala = new Bala(img.getTranslateX(),img.getY(),velocidad);
                    mapAnchorPane.getChildren().add(bala);
                    listabala.addLast(bala);
                    presionado=false;                }
            }
        });
    	TankMovement(false);
    }
    
    //tank movement
    private void TankMovement(boolean comeback) {
		TranslateTransition transition = new TranslateTransition();
		transition.setDuration(Duration.seconds(background.getTankSpeed())); //Duracion por ciclo
		transition.setCycleCount(1);  //-1 = INDEFINITE Cycle
		transition.setNode(batteryImageView); //nodo.
		transition.setOnFinished(event -> {
			if (comeback) {TankMovement(false);}
			else {TankMovement(true);}
			return;
		});
		if (comeback) {
			transition.setToX(0);
		}else {
			transition.setToX(mapAnchorPane.getPrefWidth()-batteryImageView.getFitWidth());
		}
		transition.play();
//		System.out.println(transition.getFromX());
    }
    
    //generate loop
    private void gentrTask() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(4), generate -> {
            if (Controller.background.getAlive() == 8) {
            	System.out.println("MAXIMO DE AVIONES EN PANTALLA (8)"); 
            	countText.setText(0+" s");
            	return;}
        	countdownTask();
            AirPort airport = Controller.background.searchAirPort();
            if (airport==null) {return;}
            airport.generatePlane();
            System.out.print("Plane Succesfully Created in : "); airport.print();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
    
    private void countdownTask() {
    	this.count=2;
    	Timeline cd = new Timeline(new KeyFrame(Duration.seconds(1), write -> {
            if (Controller.background.getAlive() == 8) {
            	countText.setText(0+" s");
            	return;}
    		countText.setText(count+" s");
    		this.count-=1;
    	}));
    	cd.setCycleCount(3);
    	cd.play();
    }
    
    
    //progress bar
    private void progressTask() {
    	Task<?> task = taskCreator(100);
    	powerProgressBar.progressProperty().unbind();
    	powerProgressBar.progressProperty().bind(task.progressProperty());
    	new Thread(task).start();
    }
    private Task<?> taskCreator(int sec) {
    	return new Task<Object>() {
    		@Override
    		protected Object call() throws Exception{
    			for(int i=1; i<=1000; i+=1) {
    				if(i==100) {i=98;}
    				else {
	    				if (presionado){
	    					Thread.sleep(60);
	    					updateProgress(i+1, sec);
	    				}
	    				else {
	    					i=7;
	    					updateProgress(i, sec);
	    				}
	    				setPrgColor(i+1/100);
	    			}
    			}
    			return true;
    		}
    	};
    }
   
    private void setPrgColor(double progress) {
        if (progress < 95) {
        	powerProgressBar.setStyle("-fx-accent: green");
//        } else if (progress < 66) {
//			powerProgressBar.setStyle("-fx-accent: yellow");
//        } else if (progress < 98) {
//			powerProgressBar.setStyle("-fx-accent: orange");
        } else {
        	powerProgressBar.setStyle("-fx-accent: red");
        }
    }
    
    
    
    
    
    

    
}