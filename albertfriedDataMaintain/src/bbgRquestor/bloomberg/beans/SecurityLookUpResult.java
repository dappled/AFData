package bbgRquestor.bloomberg.beans;

import java.io.Serializable;

/**
 * @author Zhenghong Dong
 */
public class SecurityLookUpResult implements Serializable {
	private static final long	serialVersionUID	= 5046583946040535251L;

	private final String		_type;
	private String				_element;
	private String				_description;
	private String				_curveExtra;
	private String				_nameElement;
	private String				_tickerElement;

	public SecurityLookUpResult(String type) {
		_type = type;
	}

	public String getType() {
		return _type;
	}

	public String getElement() {
		return _element;
	}

	public void setElement(String element) {
		this._element = element;
	}

	public String getDescription() {
		return _description;
	}

	public void setDescription(String description) {
		this._description = description;
	}

	public String getNameElement() {
		return _nameElement;
	}

	public void setNameElement(String nameElement) {
		this._nameElement = nameElement;
	}

	public String getTickerElement() {
		return _tickerElement;
	}

	public void setTickerElement(String tickerElement) {
		this._tickerElement = tickerElement;
	}

	public String getCurveExtra() {
		return _curveExtra;
	}

	public void setCurveExtra(String curveExtra) {
		this._curveExtra = curveExtra;
	}


	public void printPiece() {
		System.out.println("Type: " + getType());
		switch (_type) {
			case "Security":
				System.out.println("Security: " + getElement());
				System.out.println("Description: " + getDescription());
				break;
			case "Curve":
				System.out.println("Curve: " + getElement());
				System.out.println("Description: " + getDescription());
				System.out.println("Curve_Response_Elements: " + getCurveExtra());
				break;
			case "Govt":
				System.out.println("Parseky: " + getElement());
				System.out.println("Name: " + getNameElement());
				System.out.println("Ticker: " + getTickerElement());
				break;
			default:
				break;
		}
	}
}
