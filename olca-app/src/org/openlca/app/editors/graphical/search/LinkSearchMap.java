package org.openlca.app.editors.graphical.search;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.openlca.core.model.ProcessLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.openlca.app.tools.graphics.model.Side.INPUT;

/**
 * This is a data structure for searching a set of existing links by
 * provider and recipient processes.
 */
public class LinkSearchMap {

	/**
	 * A map process-ID -> process links, where the process is a provider means
	 * it has the product output or waste input that is described by the link.
	 */
	final TLongObjectHashMap<TIntArrayList> providerIndex;

	/**
	 * A map process-ID -> process links, where the process is connected by a
	 * product input or waste output to another process.
	 */
	TLongObjectHashMap<TIntArrayList> connectionIndex;

	ArrayList<ProcessLink> data;
	private ArrayList<Long> wasExplored = new ArrayList<>();

	public LinkSearchMap(Collection<ProcessLink> links) {
		providerIndex = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1L);
		connectionIndex = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1L);
		data = new ArrayList<>(links);
		for (int i = 0; i < data.size(); i++) {
			ProcessLink link = data.get(i);
			index(link.providerId, i, providerIndex);
			index(link.processId, i, connectionIndex);
		}
	}

	void index(long key, int val, TLongObjectHashMap<TIntArrayList> map) {
		TIntArrayList list = map.get(key);
		if (list == null) {
			list = new TIntArrayList(Constants.DEFAULT_CAPACITY, -1);
			map.put(key, list);
		}
		if (list.contains(val))
			return;
		list.add(val);
	}

	/**
	 * Returns all the links where the process with the given ID is either
	 * provider or connected with a provider.
	 */
	public List<ProcessLink> getLinks(long processId) {
		// because this method could be called quite often in graphical
		// presentations of product systems and there can be a lot of links
		// in some kind of product systems (e.g. from IO-databases) we do
		// not just merge the incoming and outgoing links here
		TIntHashSet intSet = new TIntHashSet(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);
		TIntArrayList list = providerIndex.get(processId);
		if (list != null)
			intSet.addAll(list);
		list = connectionIndex.get(processId);
		if (list != null)
			intSet.addAll(list);
		return getLinks(intSet.iterator());
	}

	/**
	 * Returns all the links where the processes with the given IDs are either
	 * provider or connected with a provider.
	 */
	public List<ProcessLink> getLinks(List<Long> processIds) {
		TIntHashSet intSet = new TIntHashSet(
				Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
		for (long processId : processIds) {
			TIntArrayList list = providerIndex.get(processId);
			if (list != null)
				intSet.addAll(list);
			list = connectionIndex.get(processId);
			if (list != null)
				intSet.addAll(list);
		}
		return getLinks(intSet.iterator());
	}

	/**
	 * Returns all links where the process with the given ID is connected (has
	 * a product input or waste output).
	 */
	public List<ProcessLink> getConnectionLinks(long processId) {
		return getLinks(processId, connectionIndex);
	}

	/**
	 * Returns all links where the process with the given ID is a provider ( has
	 * a product output or waste input).
	 */
	public List<ProcessLink> getProviderLinks(long processId) {
		return getLinks(processId, providerIndex);
	}

	private List<ProcessLink> getLinks(long processId,
			TLongObjectHashMap<TIntArrayList> map) {
		TIntArrayList list = map.get(processId);
		if (list == null)
			return Collections.emptyList();
		return getLinks(list.iterator());
	}

	private List<ProcessLink> getLinks(TIntIterator iterator) {
		List<ProcessLink> links = new ArrayList<>();
		while (iterator.hasNext()) {
			ProcessLink next = data.get(iterator.next());
			if (next != null)
				links.add(next);
		}
		return links;
	}

	public void put(ProcessLink link) {
		int index = remove(link);
		if (index == -1)
			index = getAvailableIndex();
		if (index < data.size())
			data.set(index, link);
		else
			data.add(link);
		index(link.providerId, index, providerIndex);
		index(link.processId, index, connectionIndex);
	}

	private int getAvailableIndex() {
		for (int index = 0; index < data.size(); index++)
			if (data.get(index) == null) // previously removed link
				return index;
		return data.size();
	}

	public void removeAll(Collection<ProcessLink> links) {
		for (ProcessLink link : links)
			remove(link);
	}

	public int remove(ProcessLink link) {
		int index = data.indexOf(link);
		if (index < 0)
			return -1;
		data.set(index, null);
		remove(link.providerId, index, providerIndex);
		remove(link.processId, index, connectionIndex);
		return index;
	}

	private void remove(long id, int index,
			TLongObjectHashMap<TIntArrayList> map) {
		TIntArrayList list = map.get(id);
		if (list == null)
			return;
		list.remove(index);
		if (list.size() == 0)
			map.remove(id);
	}


	/**
	 * Recursively check if any of this process's outputs or inputs chain to the
	 * reference (closed loop are not considered).
	 * Returns false is the initial process is the reference.
	 * @param process
	 * @param provider if true checks the supply chain, else the demand chain.
	 * @param ref the reference process.
	 * @return
	 */
	public boolean isChainingReference(long process, boolean provider, long ref) {
		if (wasExplored.contains(process))
			return false;
		else if (process == ref)
			// The reference node is explored if and only if it is the initial node
			// (see the condition in the for loop).
			return false;
		wasExplored.add(process);

		var links = provider
				? getConnectionLinks(process)
				: getProviderLinks(process);
		if (links.isEmpty()) {
			wasExplored.remove(process);
			return false;
		}

		for (var link : links) {
			if (link.processId == link.providerId)
				continue;
			var otherProcess = provider
					? link.providerId
					: link.processId;
			if (otherProcess == ref
					|| isChainingReference(otherProcess, provider, ref)) {
				wasExplored.remove(process);
				return true;
			}
		}
		wasExplored.remove(process);
		return false;
	}

	/**
	 * Recursively check if a process's outputs or inputs only chain to the
	 * reference process (close loop are not considered).
	 * Returns false is the initial process is the reference.
	 */
	public boolean isOnlyChainingReferenceNode(Long process, int side, Long ref) {
		if (wasExplored.contains(process))
			return false;
		else if (Objects.equals(process, ref))
			// The reference node is explored if and only if it is the initial node
			// (see the condition in the for loop).
			return false;
		wasExplored.add(process);

		var links = side == INPUT
				? getConnectionLinks(process)
				: getProviderLinks(process);
		if (links.isEmpty()) {
			wasExplored.remove(process);
			return false;
		}

		var isOnlyChainingReferenceNode = true;
		for (var link : links) {
			if (link.processId == link.providerId)
				continue;
			var otherNode = side == INPUT
					? link.providerId
					: link.processId;
			if (otherNode != ref
					&& !isOnlyChainingReferenceNode(otherNode, side, ref))
				isOnlyChainingReferenceNode = false;
		}
		wasExplored.remove(process);
		return isOnlyChainingReferenceNode;
	}

}
