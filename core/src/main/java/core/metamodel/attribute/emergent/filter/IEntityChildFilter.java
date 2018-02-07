package core.metamodel.attribute.emergent.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import core.metamodel.attribute.IAttribute;
import core.metamodel.attribute.emergent.filter.EntityChildFilterFactory.EChildFilter;
import core.metamodel.entity.IEntity;
import core.metamodel.value.IValue;

/**
 * Filter a set of entity according to value matching
 * 
 * @author kevinchapuis
 *
 * @param <E>
 */
@JsonTypeInfo(
	      use = JsonTypeInfo.Id.NAME,
	      include = JsonTypeInfo.As.EXISTING_PROPERTY,
	      property = IEntityChildFilter.TYPE
	      )
@JsonSubTypes({
	        @JsonSubTypes.Type(value = EntityOneOfMatchFilter.class),
	        @JsonSubTypes.Type(value = EntityOneOfEachMatchFilter.class),
	        @JsonSubTypes.Type(value = EntityAllMatchFilter.class)
	    })
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public interface IEntityChildFilter {
	
	public static final String COMPARATOR = "COMPARATOR";
	public static final String TYPE = "TYPE";
	
	/**
	 * Retain only entities that match values - with custom matching rule, e.g. all or one of match
	 * 
	 * @param entities
	 * @param filters
	 * @return
	 */
	public Collection<IEntity<? extends IAttribute<? extends IValue>>> retain(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, IValue... matches); 
	
	/**
	 * Chose only one entity among a collect of entities. Have to be consistent, that is the same
	 * arguments must always return the same entity
	 * 
	 * @param entities
	 * @param matches
	 * @return
	 */
	default IEntity<? extends IAttribute<? extends IValue>> choseOne(
			Collection<IEntity<? extends IAttribute<? extends IValue>>> entities, IValue... matches) {
		List<IEntity<? extends IAttribute<? extends IValue>>> retains = new ArrayList<>(this.retain(entities, matches));
		Collections.sort(retains, this.getComparator());
		return retains.get(0);
	}
	
	/**
	 * The default supplier to collect retained entities
	 * @return
	 */
	@JsonIgnore
	default Supplier<Collection<IEntity<? extends IAttribute<? extends IValue>>>> getSupplier(){
		return new Supplier<Collection<IEntity<? extends IAttribute<? extends IValue>>>>() {
			@Override
			public Collection<IEntity<? extends IAttribute<? extends IValue>>> get() {
				return new HashSet<IEntity<? extends IAttribute<? extends IValue>>>();
			}
		};
	}
	
	/**
	 * The default comparator of entities that compare entity ID
	 * @return
	 */
	@JsonIgnore
	default Comparator<IEntity<? extends IAttribute<? extends IValue>>> getDefaultComparator(){
		return new Comparator<IEntity<? extends IAttribute<? extends IValue>>>() {
			@Override
			public int compare(IEntity<? extends IAttribute<? extends IValue>> o1, 
					IEntity<? extends IAttribute<? extends IValue>> o2) {
				return o1.getEntityId().compareTo(o2.getEntityId());
			}
		};
	}
	
	@JsonProperty(COMPARATOR)
	public Comparator<IEntity<? extends IAttribute<? extends IValue>>> getComparator();
	
	public void setComparator(Comparator<IEntity<? extends IAttribute<? extends IValue>>> comparator);
	
	@JsonProperty(TYPE)
	public EChildFilter getType();
	
}
