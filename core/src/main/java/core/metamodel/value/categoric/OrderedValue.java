package core.metamodel.value.categoric;

import core.metamodel.value.IValue;
import core.util.data.GSEnumDataType;

public class OrderedValue implements IValue {

	private String value;
	private int order;
	
	private OrderedSpace sv;
	
	protected OrderedValue(OrderedSpace sv, String value, int order){
		this.sv =sv;
		this.value = value;
		this.order = order;
	}
	
	@Override
	public GSEnumDataType getType() {
		return GSEnumDataType.Order;
	}

	@Override
	public String getStringValue() {
		return value;
	}
	
	@Override
	public OrderedSpace getValueSpace() {
		return sv;
	}

	protected int compareTo(OrderedValue o) {
		return order < o.getOrder() ? -1 : order > o.getOrder() ? 1 : 0;
	}

	protected void setOrder(int order){
		this.order = order;
	}
	
	protected int getOrder() {
		return order;
	}
	
}
