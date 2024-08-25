package es;

import java.util.Calendar;
import java.util.Objects;

public class Actuador_rele {
	
	private Integer nPlaca; 
	private Integer idActuador; 
	private long timestamp;  //Calendar.getInstance().getTimeInMillis()
	private boolean activo;
	private boolean encendido;
	private int idGroup;
	
	public Actuador_rele() {
		super();	
		timestamp=Calendar.getInstance().getTimeInMillis();
		activo=false;
	}
	
	public Actuador_rele(Integer nPlaca, Integer idActuador, long timestamp, boolean activo, boolean encendido, int idGroup) {
		super();
		this.nPlaca = nPlaca;
		this.idActuador = idActuador;
		this.timestamp = timestamp;
		this.activo = activo;
		this.encendido = encendido;
		this.idGroup = idGroup;
	}



	@Override
	public int hashCode() {
		return Objects.hash(activo, encendido, idActuador, idGroup, nPlaca, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Actuador_rele other = (Actuador_rele) obj;
		return activo == other.activo && encendido == other.encendido && Objects.equals(idActuador, other.idActuador)
				&& idGroup == other.idGroup && Objects.equals(nPlaca, other.nPlaca) && timestamp == other.timestamp;
	}

	public Integer getnPlaca() {
		return nPlaca;
	}

	public void setnPlaca(Integer nPlaca) {
		this.nPlaca = nPlaca;
	}

	public Integer getIdActuador() {
		return idActuador;
	}

	public void setIdActuador(Integer idActuador) {
		this.idActuador = idActuador;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public boolean isEncendido() {
		return encendido;
	}

	public void setEncendido(boolean encendido) {
		this.encendido = encendido;
	}

	public int getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(int idGroup) {
		this.idGroup = idGroup;
	}

	@Override
	public String toString() {
		return "Actuador_Entity [nPlaca=" + nPlaca + ", idActuador=" + idActuador + ", timestamp=" + timestamp
				+ ", activo=" + activo + ", encendido=" + encendido + ", idGroup=" + idGroup + "]";
	}

	
	
}