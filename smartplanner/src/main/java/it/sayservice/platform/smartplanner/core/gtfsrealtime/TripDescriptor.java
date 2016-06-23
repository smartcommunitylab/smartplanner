/**
 * Copyright 2011-2016 SAYservice s.r.l.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from proto

package it.sayservice.platform.smartplanner.core.gtfsrealtime;

import javax.annotation.Generated;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import io.protostuff.GraphIOUtil;
import io.protostuff.Input;
import io.protostuff.Message;
import io.protostuff.Output;
import io.protostuff.Schema;


@Generated("java_bean")
public final class TripDescriptor implements Externalizable, Message<TripDescriptor>, Schema<TripDescriptor>
{
    @Generated("java_bean")
    public enum ScheduleRelationship implements io.protostuff.EnumLite<ScheduleRelationship>
    {
        SCHEDULED(0),
        ADDED(1),
        UNSCHEDULED(2),
        CANCELED(3);
        
        public final int number;
        
        private ScheduleRelationship (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static ScheduleRelationship valueOf(int number)
        {
            switch(number) 
            {
                case 0: return SCHEDULED;
                case 1: return ADDED;
                case 2: return UNSCHEDULED;
                case 3: return CANCELED;
                default: return null;
            }
        }
    }


    public static Schema<TripDescriptor> getSchema()
    {
        return DEFAULT_INSTANCE;
    }

    public static TripDescriptor getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    static final TripDescriptor DEFAULT_INSTANCE = new TripDescriptor();

    
    private String tripId;
    private String startTime;
    private String startDate;
    private ScheduleRelationship scheduleRelationship;
    private String routeId;
    private Integer directionId;

    public TripDescriptor()
    {

    }

    // getters and setters

    // tripId

    public String getTripId()
    {
        return tripId;
    }


    public void setTripId(String tripId)
    {
        this.tripId = tripId;
    }

    // startTime

    public String getStartTime()
    {
        return startTime;
    }


    public void setStartTime(String startTime)
    {
        this.startTime = startTime;
    }

    // startDate

    public String getStartDate()
    {
        return startDate;
    }


    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    // scheduleRelationship

    public ScheduleRelationship getScheduleRelationship()
    {
        return scheduleRelationship;
    }


    public void setScheduleRelationship(ScheduleRelationship scheduleRelationship)
    {
        this.scheduleRelationship = scheduleRelationship;
    }

    // routeId

    public String getRouteId()
    {
        return routeId;
    }


    public void setRouteId(String routeId)
    {
        this.routeId = routeId;
    }

    // directionId

    public Integer getDirectionId()
    {
        return directionId;
    }


    public void setDirectionId(Integer directionId)
    {
        this.directionId = directionId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final TripDescriptor that = (TripDescriptor) obj;
        return
                Objects.equals(this.tripId, that.tripId) &&
                Objects.equals(this.startTime, that.startTime) &&
                Objects.equals(this.startDate, that.startDate) &&
                Objects.equals(this.scheduleRelationship, that.scheduleRelationship) &&
                Objects.equals(this.routeId, that.routeId) &&
                Objects.equals(this.directionId, that.directionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, startTime, startDate, scheduleRelationship, routeId, directionId);
    }

    @Override
    public String toString() {
        return "TripDescriptor{" +
                    "tripId=" + tripId +
                    ", startTime=" + startTime +
                    ", startDate=" + startDate +
                    ", scheduleRelationship=" + scheduleRelationship +
                    ", routeId=" + routeId +
                    ", directionId=" + directionId +
                '}';
    }
    // java serialization

    public void readExternal(ObjectInput in) throws IOException
    {
        GraphIOUtil.mergeDelimitedFrom(in, this, this);
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        GraphIOUtil.writeDelimitedTo(out, this, this);
    }

    // message method

    public Schema<TripDescriptor> cachedSchema()
    {
        return DEFAULT_INSTANCE;
    }

    // schema methods

    public TripDescriptor newMessage()
    {
        return new TripDescriptor();
    }

    public Class<TripDescriptor> typeClass()
    {
        return TripDescriptor.class;
    }

    public String messageName()
    {
        return TripDescriptor.class.getSimpleName();
    }

    public String messageFullName()
    {
        return TripDescriptor.class.getName();
    }

    public boolean isInitialized(TripDescriptor message)
    {
        return true;
    }

    public void mergeFrom(Input input, TripDescriptor message) throws IOException
    {
        for(int number = input.readFieldNumber(this);; number = input.readFieldNumber(this))
        {
            switch(number)
            {
                case 0:
                    return;
                case 1:
                    message.tripId = input.readString();
                    break;
                case 2:
                    message.startTime = input.readString();
                    break;
                case 3:
                    message.startDate = input.readString();
                    break;
                case 4:
                    message.scheduleRelationship = ScheduleRelationship.valueOf(input.readEnum());
                    break;

                case 5:
                    message.routeId = input.readString();
                    break;
                case 6:
                    message.directionId = input.readUInt32();
                    break;
                default:
                    input.handleUnknownField(number, this);
            }   
        }
    }


    public void writeTo(Output output, TripDescriptor message) throws IOException
    {
        if(message.tripId != null)
            output.writeString(1, message.tripId, false);

        if(message.startTime != null)
            output.writeString(2, message.startTime, false);

        if(message.startDate != null)
            output.writeString(3, message.startDate, false);

        if(message.scheduleRelationship != null)
             output.writeEnum(4, message.scheduleRelationship.number, false);

        if(message.routeId != null)
            output.writeString(5, message.routeId, false);

        if(message.directionId != null)
            output.writeUInt32(6, message.directionId, false);
    }

    public String getFieldName(int number)
    {
        return Integer.toString(number);
    }

    public int getFieldNumber(String name)
    {
        return Integer.parseInt(name);
    }
    

}
