package es;

import java.util.Calendar;
import java.util.Objects;

public class SensorHumedad {
	private Integer idSensor;
	private Integer nPlaca;
	private Float humedad; 
	private Long timestamp; //Calendar.getInstance().getTimeInMillis()
	private Float temperatura;
	private int idGroup;
	
	public SensorHumedad(Integer idSensor, Integer nPlaca, Float humedad, Long timestamp, Float temperatura, Integer idGroup) {
		super();
		this.idSensor=idSensor;
		this.nPlaca=nPlaca;
		this.humedad=humedad;
		this.timestamp=timestamp;
		this.temperatura=temperatura;
		this.idGroup=idGroup;
	}
	
	@Override
	public String toString() {
		return "SensorHumedad [idSensor=" + idSensor + ", nPlaca=" + nPlaca + ", humedad=" + humedad + ", timestamp="
				+ timestamp + ", temperatura=" + temperatura + ", idGroup=" + idGroup + "]";
	}

	public SensorHumedad() {
		super();	
		timestamp=Calendar.getInstance().getTimeInMillis();
		humedad=0.0f;
		temperatura= 0.0f;
	}

	public Integer getIdSensor() {
		return idSensor;
	}

	public void setIdSensor(Integer idSensor) {
		this.idSensor = idSensor;
	}

	public Integer getnPlaca() {
		return nPlaca;
	}

	public void setnPlaca(Integer nPlaca) {
		this.nPlaca = nPlaca;
	}

	public Float getHumedad() {
		return humedad;
	}

	public void setHumedad(Float humedad) {
		this.humedad = humedad;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Float getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(Float temperatura) {
		this.temperatura = temperatura;
	}

	public int getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(int idGroup) {
		this.idGroup = idGroup;
	}

	@Override
	public int hashCode() {
		return Objects.hash(humedad, idGroup, idSensor, nPlaca, temperatura, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorHumedad other = (SensorHumedad) obj;
		return Objects.equals(humedad, other.humedad) && idGroup == other.idGroup
				&& Objects.equals(idSensor, other.idSensor) && Objects.equals(nPlaca, other.nPlaca)
				&& Objects.equals(temperatura, other.temperatura) && Objects.equals(timestamp, other.timestamp);
	}


}
