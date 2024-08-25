package es;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.stream.IntStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.List;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class RestServer extends AbstractVerticle {


	MySQLPool mySqlClient;
	private Gson gson;

    MqttClient mqttClient;

	public void start(Promise<Void> startFuture) {
		// Creating some synthetic data
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("localhost")
				.setDatabase("dadproject").setUser("root").setPassword("root");

		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
		
		// Defining the router object
		Router router = Router.router(vertx);
        // Creating some synthetic data
        //createSomeData(25);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        
        vertx.createHttpServer().requestHandler(router::handle).listen(8081, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
        
        mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "localhost", s -> {});

      router.route("/api/sensor*").handler(BodyHandler.create());
      //GETS SENSORES
      router.get("/api/sensor").handler(this::getAllWithConnectionSen);//CHECK
      router.get("/api/sensor/all").handler(this::getAllSensors); //CHECK
      router.get("/api/sensor/:id").handler(this::getByIDSen); 
      router.get("/api/sensor/:id/last").handler(this::getLastIDSen);
      router.get("/api/sensor/:id/group").handler(this::getLastIdGroupSen);
      
      //MODIFICADORES DE SENSORES
      router.post("/api/sensor").handler(this::addSensor);//CHECK
      router.delete("/api/sensor/:id").handler(this::deleteSensor);//CHECK
      router.put("/api/sensor/:id").handler(this::updateSensor);
      
      
      //GET DE ACTUADORES
      router.route("/api/actuador*").handler(BodyHandler.create());
      router.get("/api/actuador").handler(this::getAllWithConnectionAct);//check
      router.get("/api/actuador/all").handler(this::getAllActuadores);//check
      router.get("/api/actuador/:id").handler(this::getByIDAct); 
      router.get("/api/actuador/:id/last").handler(this::getLastIDAct); 
      router.get("/api/actuador/:id/group").handler(this::getLastidGroupAct); 
      
      //MODIFICADORES DE ACTUADORES
      router.post("/api/actuador").handler(this::addActuador); 
      router.delete("/api/actuador/:id").handler(this::deleteActuador); 
      router.put("/api/actuador/:id").handler(this::updateActuador);
        
		// Handling any server startup result

	}

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        try {          
//        	sensors.clear();
            stopPromise.complete();
        } catch (Exception e) {
            stopPromise.fail(e);
        }
        super.stop(stopPromise);
    }
		 // Sensor Endpoints
    private void getAllSensors(RoutingContext routingContext) {
        mySqlClient.query("SELECT * FROM dadproject.sensor;").execute(res -> {
            if (res.succeeded()) {
                // Get the result set
                RowSet<Row> resultSet = res.result();
                List<List<Object>> result = new ArrayList<>();
                for (Row elem : resultSet) {
                    List<Object> sensorData = new ArrayList<>();
                    sensorData.add(elem.getInteger("idSensor"));
                    sensorData.add(elem.getInteger("nPlaca"));
                    sensorData.add(elem.getFloat("humedad"));
                    sensorData.add(elem.getLong("timestamp"));
                    sensorData.add(elem.getFloat("temperatura"));
                    result.add(sensorData);
                }
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(gson.toJson(result));
            } else {
            	System.out.println("Error: " + res.cause().getLocalizedMessage());
	              routingContext.response()
	                .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(404)
	                        .end("Error al obtener los sensores: " + res.cause().getMessage());
            }
        });
    }

		private void getAllWithConnectionSen(RoutingContext routingContext) {
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().query("SELECT * FROM dadproject.sensor;").execute(res -> {
						if (res.succeeded()) {
							// Get the result set
							RowSet<Row> resultSet = res.result();
							System.out.println(resultSet.size());
							List<SensorHumedad> result = new ArrayList<>();
							for (Row elem : resultSet) {
								result.add(new SensorHumedad(elem.getInteger("idSensor"), elem.getInteger("nPlaca"), elem.getFloat("humedad"),
										elem.getLong("timestamp"), elem.getFloat("temperatura"), elem.getInteger("idGroup")));
							}
			                routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end(gson.toJson(result));
						} else {
							System.out.println("Error: " + res.cause().getLocalizedMessage());
							 routingContext.response()
				                .putHeader("content-type", "application/json; charset=utf-8")
				                        .setStatusCode(404)
				                        .end("Error al obtener los sensores: " + res.cause().getMessage());
						}
						connection.result().close();
					});
				} else {
					System.out.println(connection.cause().toString());
					 routingContext.response()
		                .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("Error al obtener los sensores: " + connection.cause().getMessage());
				}
			});
		}
		
		private void getByIDSen(RoutingContext routingContext) {
			mySqlClient.getConnection(connection -> {
				int idSensor = Integer.parseInt(routingContext.request().getParam("ID")); //porque solo accede si lo llamamos ID?
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM dadproject.sensor WHERE idSensor = ?").execute(
							Tuple.of(idSensor), res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									System.out.println(resultSet.size());
									List<SensorHumedad> result = new ArrayList<>();
									for (Row elem : resultSet) {
										result.add(new SensorHumedad(elem.getInteger("idSensor"), elem.getInteger("nPlaca"), elem.getFloat("humedad"),
												elem.getLong("timestamp"), elem.getFloat("temperatura"), elem.getInteger("idGroup")));
									}
					                routingContext.response()
			                        .putHeader("content-type", "application/json; charset=utf-8")
			                        .setStatusCode(200)
			                        .end(gson.toJson(result));
								} else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
									routingContext.response().setStatusCode(500).end("Error al obtener los sensores: " + res.cause().getMessage());
								}
								connection.result().close();
							});
				} else {
					System.out.println(connection.cause().toString());
					routingContext.response().setStatusCode(500).end("Error con la coenxión: " + connection.cause().getMessage());
				}
			});
		}

//		private Date localDateToDateSen(LocalDate localDate) {
//			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//		}
		
		private void getLastIDSen(RoutingContext routingContext) {
		    Integer idSensor = Integer.parseInt(routingContext.request().getParam("ID"));
		    mySqlClient.getConnection(connection -> {
		        if (connection.succeeded()) {
		            connection.result().preparedQuery("SELECT * FROM dadproject.sensor WHERE idSensor = ? ORDER BY timestamp DESC LIMIT 1")
		                    .execute(Tuple.of(idSensor), res -> {
		                        if (res.succeeded()) {
		                            // Get the result set
		                            RowSet<Row> resultSet = res.result();
		                            List<SensorHumedad> result = new ArrayList<>();
									for (Row elem : resultSet) {
										result.add(new SensorHumedad(elem.getInteger("idSensor"), elem.getInteger("nPlaca"), elem.getFloat("humedad"),
												elem.getLong("timestamp"), elem.getFloat("temperatura"), elem.getInteger("idGroup")));
									}
					                routingContext.response()
			                        .putHeader("content-type", "application/json; charset=utf-8")
			                        .setStatusCode(200)
			                        .end(gson.toJson(result));
		                        } else {
		                            System.out.println("Error: " + res.cause().getLocalizedMessage());
		                            routingContext.response()
		                                    .setStatusCode(404)
		                                    .end("Error al obtener el sensor con idSensor " + idSensor + ": " + res.cause().getMessage());
		                        }
		                        connection.result().close();
		                    });
		        } else {
		            System.out.println(connection.cause().toString());
		            routingContext.response()
		                    .setStatusCode(500)
		                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
		        }
		    });
		}
		
		
		private void getLastIdGroupSen(RoutingContext routingContext) {
		    int idGroup = Integer.parseInt(routingContext.request().getParam("ID"));
		    mySqlClient.getConnection(connection -> {
		        if (connection.succeeded()) {
		            connection.result().preparedQuery("SELECT * FROM dadproject.sensor WHERE idGroup = ? ORDER BY timestamp DESC LIMIT 1")
		                    .execute(Tuple.of(idGroup), res -> {
		                        if (res.succeeded()) {
		                            // Get the result set
		                            RowSet<Row> resultSet = res.result();
		                            List<SensorHumedad> result = new ArrayList<>();
									for (Row elem : resultSet) {
										result.add(new SensorHumedad(elem.getInteger("idSensor"), elem.getInteger("nPlaca"), elem.getFloat("humedad"),
												elem.getLong("timestamp"), elem.getFloat("temperatura"), elem.getInteger("idGroup")));
									}
					                routingContext.response()
			                        .putHeader("content-type", "application/json; charset=utf-8")
			                        .setStatusCode(200)
			                        .end(gson.toJson(result));
		                        } else {
		                            System.out.println("Error: " + res.cause().getLocalizedMessage());
		                            routingContext.response()
		                                    .setStatusCode(404)
		                                    .end("Error al obtener el sensor con idGroup " + idGroup + ": " + res.cause().getMessage());
		                        }
		                        connection.result().close();
		                    });
		        } else {
		            System.out.println(connection.cause().toString());
		            routingContext.response()
		                    .setStatusCode(500)
		                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
		        }
		    });
		}
		
		
//		
		private void addSensor(RoutingContext routingContext) {

		    // Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_humedad_Entity
		    final SensorHumedad sensor = gson.fromJson(routingContext.getBodyAsString(),
		    		SensorHumedad.class);

		    // Ejecutamos la inserción en la base de datos MySQL
		    mySqlClient
		            .preparedQuery(
		                    "INSERT INTO sensor (idSensor, nPlaca, humedad, timestamp, temperatura, idGroup) VALUES (?, ?, ?, ?, ?, ?)")
		            .execute((Tuple.of(sensor.getIdSensor(), sensor.getnPlaca(), sensor.getHumedad(), sensor.getTimestamp(),
		                    sensor.getTemperatura(), sensor.getIdGroup())), res -> {
		                        if (res.succeeded()) {
		                            // Si la inserción es exitosa, respondemos con el sensor creado
		                            routingContext.response().setStatusCode(201).putHeader("content-type",
		                                    "application/json; charset=utf-8").end("Sensor añadido correctamente");

		                            // Publicar en MQTT después de la inserción exitosa
		                            if (sensor.getTemperatura() > 30) {
		                                mqttClient.publish(sensor.getIdGroup() + "",
		                                        Buffer.buffer("ON"), MqttQoS.AT_LEAST_ONCE, false, false);
		                            } else {
		                                mqttClient.publish(sensor.getIdGroup() + "",
		                                        Buffer.buffer("OFF"), MqttQoS.AT_LEAST_ONCE, false, false);
		                            }
		                        } else {
		                            // Si hay un error en la inserción, respondemos con el mensaje de error
		                            System.out.println("Error: " + res.cause().getLocalizedMessage());
		                            routingContext.response().setStatusCode(500).end("Error al añadir el sensor: " + res.cause().getMessage());
		                        }
		                    });

		}

	    
	    
	    private void deleteSensor(RoutingContext routingContext) {
	        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
	        int ID = Integer.parseInt(routingContext.request().getParam("ID"));
	        
	        // Ejecutamos la eliminación en la base de datos MySQL
	        mySqlClient.preparedQuery("DELETE FROM sensor WHERE idSensor = ?").execute((Tuple.of(ID)), res -> {
	                if (res.succeeded()) {
	                    // Si la eliminación es exitosa, respondemos con el sensor eliminado
	                    if (res.result().rowCount() > 0) {
	                        routingContext.response()
	                            .setStatusCode(200)
	                            .putHeader("content-type", "application/json; charset=utf-8")
	                            .end(gson.toJson(new JsonObject().put("message", "Sensor eliminado correctamente")));
	                    } 
	                } else {
	                    // Si hay un error en la eliminación, respondemos con el código 500 (Error interno del servidor)
	                	System.out.println("Error: " + res.cause().getLocalizedMessage());
			            routingContext.response()
			                    .setStatusCode(500)
			                    .end("Error al conectar con la base de datos: ");
	                }
	            });
	    }
		//ADD=PUSH
		//get=SELECT
		/////////////////////////////////////////////////////////
	    private void updateSensor(RoutingContext routingContext) {
	        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
	        int id = Integer.parseInt(routingContext.request().getParam("ID"));
	        
	        // Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
	        final SensorHumedad updatedSensor = gson.fromJson(routingContext.getBodyAsString(), SensorHumedad.class);
	        
	        // Ejecutamos la actualización en la base de datos MySQL
	        mySqlClient.preparedQuery("UPDATE sensor SET humedad = ?, timestamp = ?, temperatura = ? WHERE idSensor = ?").execute( 
	        		(Tuple.of(updatedSensor.getTimestamp(), updatedSensor.getHumedad(), updatedSensor.getTemperatura(), id)), res -> {
	                if (res.succeeded()) {
	                    // Si la actualización es exitosa, respondemos con el sensor actualizado
	                    if (res.result().rowCount() > 0) {
	                        routingContext.response()
	                            .setStatusCode(200)
	                            .putHeader("content-type", "application/json; charset=utf-8")
	                            .end(gson.toJson(updatedSensor));
	                    } 
	                } else {
	                    // Si hay un error en la actualización, respondemos con el código 500 (Error interno del servidor)
	                	System.out.println("Error: " + res.cause().getLocalizedMessage());
		  	              routingContext.response()
		  	                .putHeader("content-type", "application/json; charset=utf-8")
		  	                        .setStatusCode(404)
		  	                        .end("Error al actualizar los sensores: " + res.cause().getMessage());
	                }
	            });
	    }
	    
	    
	    
	    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	    //ACTUDORES

	    private void createSomeData(int number) {
	        Random rnd = new Random();
	        IntStream.range(0, number).forEach(elem -> {
	            int id = elem + 1; // Ajustamos el ID para que comience en 1 en lugar de 0
	            int nPlaca = rnd.nextInt();
	            long timestamp = Calendar.getInstance().getTimeInMillis() + rnd.nextInt(1000); // Agregamos un número aleatorio al timestamp
	            float humedad = rnd.nextFloat() * 100; // Generamos un valor aleatorio entre 0 y 100 para la humedad
	            float temperatura = rnd.nextFloat() * 50; // Generamos un valor aleatorio entre 0 y 50 para la temperatura
	            
	            // Ejecutamos la inserción en la base de datos MySQL
	            mySqlClient.preparedQuery("INSERT INTO sensor (idSensor, nPlaca, humedad, timestamp, temperatura) VALUES (?, ?, ?, ?, ?)").execute(
	            		(Tuple.of(id, nPlaca, timestamp, humedad, temperatura)), ar -> {
	                    if (ar.succeeded()) {
	                        System.out.println("Se ha insertado un nuevo sensor en la base de datos.");
	                    } else {
	                        System.err.println("Error " + ar.cause().getMessage());
	                    }
	                });
	        });
	    }
	    

		 // Actuador Endpoints
			private void getAllActuadores(RoutingContext routingContext) {
				//RoutingContext routingContext PARAMETRO DE LA FUNCION
//				routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
//						.end(gson.toJson(new UserEntityListWrapper(users.values())));
					mySqlClient.query("SELECT * FROM dadproject.actuador;").execute(res -> {
						if (res.succeeded()) {
							// Get the result set
							RowSet<Row> resultSet = res.result();
							System.out.println(resultSet.size());
							List<Actuador_rele> result = new ArrayList<>();
							for (Row elem : resultSet) {
								result.add(new Actuador_rele(elem.getInteger("nPlaca"), elem.getInteger("idActuador")
										, elem.getLong("timestamp"), elem.getBoolean("activo")
										,elem.getBoolean("encendido"), elem.getInteger("idGroup")));
							}
			                routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end(gson.toJson(result));
						} else {
							System.out.println("Error: " + res.cause().getLocalizedMessage());
				              routingContext.response()
				                .putHeader("content-type", "application/json; charset=utf-8")
				                        .setStatusCode(404)
				                        .end("Error al obtener los actuadores: " + res.cause().getMessage());
						}
					});
			}
				private void getAllWithConnectionAct(RoutingContext routingContext) {
					mySqlClient.getConnection(connection -> {
						if (connection.succeeded()) {
							connection.result().query("SELECT * FROM dadproject.actuador;").execute(res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									System.out.println(resultSet.size());
									List<Actuador_rele> result = new ArrayList<>();
									for (Row elem : resultSet) {
										result.add(new Actuador_rele(elem.getInteger("nPlaca"), elem.getInteger("idActuador")
												, elem.getLong("timestamp"), elem.getBoolean("activo")
												,elem.getBoolean("encendido"), elem.getInteger("idGroup")));
									}
					                routingContext.response()
			                        .putHeader("content-type", "application/json; charset=utf-8")
			                        .setStatusCode(200)
			                        .end(gson.toJson(result));								} else {
									System.out.println("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
						} else {
							System.out.println(connection.cause().toString());
				            routingContext.response()
				                    .setStatusCode(500)
				                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
						}
					});
				}
				
				private void getByIDAct(RoutingContext routingContext) {
					mySqlClient.getConnection(connection -> {
			            int ID = Integer.parseInt(routingContext.request().getParam("ID"));
						if (connection.succeeded()) {
							connection.result().preparedQuery("SELECT * FROM dadproject.actuador WHERE nPlaca = ?").execute(
									Tuple.of(ID), res -> {
										if (res.succeeded() && res.result().size()!=0) {
											// Get the result set
											RowSet<Row> resultSet = res.result();
											System.out.println(resultSet.size());
											List<Actuador_rele> result = new ArrayList<>();
											for (Row elem : resultSet) {
												result.add(new Actuador_rele(elem.getInteger("nPlaca"), elem.getInteger("idActuador")
														, elem.getLong("timestamp"), elem.getBoolean("activo")
														,elem.getBoolean("encendido"), elem.getInteger("idGroup")));
											}
							                routingContext.response()
					                        .putHeader("content-type", "application/json; charset=utf-8")
					                        .setStatusCode(200)
					                        .end(gson.toJson(result));										
							                }  else {
						                        if (res.cause() != null) {
						                            System.out.println("Error: " + res.cause().getLocalizedMessage());
						                        } else {
						                            System.out.println("Unknown error occurred.");
						                        }
						                    }
						                    if (connection.result() != null) {
						                        connection.result().close();
						                    }
									});
						} else {
							 System.out.println(connection.cause().toString());
					            routingContext.response()
					                    .setStatusCode(500)
					                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
						}
					});
				}

//				private Date localDateToDate(LocalDate localDate) {
//					return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
//				}
				
				private void getLastIDAct(RoutingContext routingContext) {
				    int idActuador = Integer.parseInt(routingContext.request().getParam("ID")); //idActuador lo tenemos como nPlaca
				    mySqlClient.getConnection(connection -> {
				        if (connection.succeeded()) {
				            connection.result().preparedQuery("SELECT * FROM dadproject.actuador WHERE nPlaca = ? ORDER BY timestamp DESC LIMIT 1")
				                    .execute(Tuple.of(idActuador), res -> {
				                        if (res.succeeded() && res.result().size() != 0) {
				                            // Get the result set
				                            RowSet<Row> resultSet = res.result();
				                            List<Actuador_rele> result = new ArrayList<>();
											for (Row elem : resultSet) {
												result.add(new Actuador_rele(elem.getInteger("nPlaca"), elem.getInteger("idActuador")
														, elem.getLong("timestamp"), elem.getBoolean("activo")
														,elem.getBoolean("encendido"), elem.getInteger("idGroup")));
											}
							                routingContext.response()
					                        .putHeader("content-type", "application/json; charset=utf-8")
					                        .setStatusCode(200)
					                        .end(gson.toJson(result));
				                        } else {
				                            System.out.println("Error: " + res.cause().getLocalizedMessage());
				                            routingContext.response()
				                                    .setStatusCode(404)
				                                    .end("Error al obtener el actuador con idActuador " + idActuador + ": " + res.cause().getMessage());
				                        }
				                        connection.result().close();
				                    });
				        } else {
				            System.out.println(connection.cause().toString());
				            routingContext.response()
				                    .setStatusCode(500)
				                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
				        }
				    });
				}

				
				private void getLastidGroupAct(RoutingContext routingContext) {
				    int idGroup = Integer.parseInt(routingContext.request().getParam("ID"));
				    mySqlClient.getConnection(connection -> {
				        if (connection.succeeded()) {
				            connection.result().preparedQuery("SELECT * FROM dadproject.actuador WHERE idGroup = ? ORDER BY timestamp DESC LIMIT 1")
				                    .execute(Tuple.of(idGroup), res -> {
				                        if (res.succeeded() && res.result().size() != 0) {
				                            // Get the result set
				                            RowSet<Row> resultSet = res.result();
				                            List<Actuador_rele> result = new ArrayList<>();
											for (Row elem : resultSet) {
												result.add(new Actuador_rele(elem.getInteger("nPlaca"), elem.getInteger("idActuador")
														, elem.getLong("timestamp"), elem.getBoolean("activo")
														,elem.getBoolean("encendido"), elem.getInteger("idGroup")));
											}
											
							                routingContext.response()
					                        .putHeader("content-type", "application/json; charset=utf-8")
					                        .setStatusCode(200)
					                        .end(gson.toJson(result));
				                        } else {
				                            System.out.println("Error: " + res.cause().getLocalizedMessage());
				                            routingContext.response()
				                                    .setStatusCode(404)
				                                    .end("Error al obtener el actuador con idGroup " + idGroup + ": " + res.cause().getMessage());
				                        }
				                        connection.result().close();
				                    });
				        } else {
				            System.out.println(connection.cause().toString());
				            routingContext.response()
				                    .setStatusCode(500)
				                    .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
				        }
				    });
				}
				
				
				
				
				
				
				
				
				//ADD=PUSH
				//get=SELECT
				/////////////////////////////////////////////////////////
					
				    
				    private void addActuador(RoutingContext routingContext) {
				    	
				        // Parseamos el cuerpo de la solicitud HTTP a un objeto Sensor_humedad_Entity
				        final Actuador_rele actuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_rele.class);
				        System.out.println(actuador);
				        // Ejecutamos la inserción en la base de datos MySQL
				        mySqlClient.preparedQuery("INSERT INTO actuador (nPlaca, idActuador, timestamp, activo, encendido, idGroup) VALUES (?, ?, ?, ?, ?, ?)").execute(
				        		(Tuple.of(actuador.getnPlaca(), actuador.getIdActuador(), actuador.getTimestamp(),actuador.isActivo(),actuador.isEncendido(), actuador.getIdGroup())), res -> {
				                if (res.succeeded()) {
				                    // Si la inserción es exitosa, respondemos con el sensor creado
				                    routingContext.response()
				                        .setStatusCode(201)
				                        .putHeader("content-type", "application/json; charset=utf-8")
				                        .end(gson.toJson(new JsonObject().put("message", "Actuador añadido correctamente")));
				                } else {
				                    // Si hay un error en la inserción, respondemos con el mensaje de error
				                	System.out.println("Error: " + res.cause().getLocalizedMessage());
						            routingContext.response()
						                    .setStatusCode(500)
						                    .end("Error al conectar con la base de datos: ");
				                	}
				            });
						mqttClient.publish("topic_1", Buffer.buffer(actuador.toString()), MqttQoS.AT_LEAST_ONCE, false, false);

				    }

				    
				    private void deleteActuador(RoutingContext routingContext) {
				        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
				        int id = Integer.parseInt(routingContext.request().getParam("ID"));
				        
				        // Ejecutamos la eliminación en la base de datos MySQL
				        mySqlClient.preparedQuery("DELETE FROM actuador WHERE idActuador = ?").execute( (Tuple.of(id)), res -> {
				                if (res.succeeded()) {
				                    // Si la eliminación es exitosa, respondemos con el sensor eliminado
				                    if (res.result().rowCount() > 0) {
				                        routingContext.response()
				                            .setStatusCode(200)
				                            .putHeader("content-type", "application/json; charset=utf-8")
				                            .end(gson.toJson(new JsonObject().put("message", "Sensor eliminado correctamente")));
				                    } 
				                } else {
				                    // Si hay un error en la eliminación, respondemos con el código 500 (Error interno del servidor)
				                	System.out.println("Error: " + res.cause().getLocalizedMessage());
						            routingContext.response()
						                    .setStatusCode(500)
						                    .end("Error al conectar con la base de datos: ");
				                }
				            });
				    }

				    
				    private void updateActuador(RoutingContext routingContext) {
				        // Obtenemos el ID del sensor de los parámetros de la solicitud HTTP
				        int idActuador = Integer.parseInt(routingContext.request().getParam("ID"));
				        
				        // Obtenemos el sensor actualizado del cuerpo de la solicitud HTTP
				        final Actuador_rele updatedActuador = gson.fromJson(routingContext.getBodyAsString(), Actuador_rele.class);
				        
				        // Ejecutamos la actualización en la base de datos MySQL
				        mySqlClient.preparedQuery("UPDATE actuador SET timestamp = ?, activo = ?, encendido = ? WHERE nPlaca = ?").execute(
				        		(Tuple.of(updatedActuador.getTimestamp(), updatedActuador.isActivo(), updatedActuador.isEncendido(), idActuador)), res -> {
				                if (res.succeeded()) {
				                    // Si la actualización es exitosa, respondemos con el sensor actualizado
				                    if (res.result().rowCount() > 0) {
				                        routingContext.response()
				                            .setStatusCode(200)
				                            .putHeader("content-type", "application/json; charset=utf-8")
				                            .end(gson.toJson(updatedActuador));
				                    } 
				                } else {
				                    // Si hay un error en la actualización, respondemos con el código 500 (Error interno del servidor)
				                	System.out.println("Error: " + res.cause().getLocalizedMessage());
					  	              routingContext.response()
					  	                .putHeader("content-type", "application/json; charset=utf-8")
					  	                        .setStatusCode(404)
					  	                        .end("Error al actualizar los actudores: " + res.cause().getMessage());
				                }
				            });
				    }

			   
				    // crear un sensor

			  
			    

			}