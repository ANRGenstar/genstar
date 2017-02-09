package spin.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class Tools {
	/** Ajoute, dans une hashtable(key, arraylist<value>) une valeur, que la key existe
	 * déjà ou non. 
	 * 
	 * @param table
	 * @param key
	 * @param value
	 * @return true si la key existait déjé, false sinon.
	 */
	public static <T1 extends Object, T2 extends Object> boolean addElementInHashArray(Hashtable<T1,ArrayList<T2>> table,T1 key, T2 value){
		if(table.containsKey(key)){
			table.get(key).add(value);
			return true;
		}else
		{
			table.put(key, new ArrayList<T2>(Arrays.asList(value)));
			return false;
		}
	}
}
