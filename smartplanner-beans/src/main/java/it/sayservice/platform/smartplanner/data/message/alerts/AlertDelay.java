/**
 *    Copyright 2011-2016 SAYservice s.r.l.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package it.sayservice.platform.smartplanner.data.message.alerts;

import it.sayservice.platform.smartplanner.data.message.EffectType;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.Transport;

public class AlertDelay extends Alert {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2156619413873067751L;
	private Position position;
	private long delay;
	
	public Position getPosition() {
		return position;
	}
	public void setPosition(Position position) {
		this.position = position;
	}
	public long getDelay() {
		return delay;
	}
	public void setDelay(long delay) {
		this.delay = delay;
	}
	
	public static void main(String[] args) {
		String effect = "detour";
		EffectType effectType = EffectType.UNKNOWN_EFFECT;
		try {
			effectType = EffectType.valueOf(effect);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("effectType: "+effectType);
	}
}
