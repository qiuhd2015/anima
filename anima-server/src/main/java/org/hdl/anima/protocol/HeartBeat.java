package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;
/**
 * HeartBeat message.
 * @author q
 *
 */
public class HeartBeat implements Record {

	private boolean twoWay;
	
	@Override
	public void serialize(OutputArchive output) throws IOException {
		output.writeBool(twoWay);
	}

	@Override
	public void deserialize(InputArchive input) throws IOException {
		this.twoWay = input.readBool();
	}

	public boolean isTwoWay() {
		return twoWay;
	}

	public void setTwoWay(boolean twoWay) {
		this.twoWay = twoWay;
	}
}
