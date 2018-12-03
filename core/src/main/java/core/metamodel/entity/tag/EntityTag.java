package core.metamodel.entity.tag;

/**
 * Tag associated to entity in order to capture relationship with sub-entities of a super entity
 * 
 * @author kevinchapuis
 *
 */
public enum EntityTag { 
	
	HHHead(EntityTag.UPWARD),
	Parent(EntityTag.HORIZONTAL),
	Child(EntityTag.HORIZONTAL);
	
	public static final int UPWARD = 1;
	public static final int HORIZONTAL = 0;
	public static final int DOWNWARD = -1;
	
	private int layer;

	private EntityTag(int layer) {
		this.layer = layer;
	}
	
	public int getLayer() {
		return layer;
	}
	
}
