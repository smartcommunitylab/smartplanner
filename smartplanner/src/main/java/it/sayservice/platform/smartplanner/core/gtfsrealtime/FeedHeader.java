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

import io.protostuff.UninitializedMessageException;
@Generated("java_bean")
public final class FeedHeader implements Externalizable, Message<FeedHeader>, Schema<FeedHeader>
{
    @Generated("java_bean")
    public enum Incrementality implements io.protostuff.EnumLite<Incrementality>
    {
        FULL_DATASET(0),
        DIFFERENTIAL(1);
        
        public final int number;
        
        private Incrementality (int number)
        {
            this.number = number;
        }
        
        public int getNumber()
        {
            return number;
        }
        
        public static Incrementality valueOf(int number)
        {
            switch(number) 
            {
                case 0: return FULL_DATASET;
                case 1: return DIFFERENTIAL;
                default: return null;
            }
        }
    }


    public static Schema<FeedHeader> getSchema()
    {
        return DEFAULT_INSTANCE;
    }

    public static FeedHeader getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    static final FeedHeader DEFAULT_INSTANCE = new FeedHeader();

    static final Incrementality DEFAULT_INCREMENTALITY = Incrementality.FULL_DATASET;
    
    private String gtfsRealtimeVersion;
    private Incrementality incrementality = DEFAULT_INCREMENTALITY;
    private Long timestamp;

    public FeedHeader()
    {

    }

    public FeedHeader(
        String gtfsRealtimeVersion
    )
    {
        this.gtfsRealtimeVersion = gtfsRealtimeVersion;
    }

    // getters and setters

    // gtfsRealtimeVersion

    public String getGtfsRealtimeVersion()
    {
        return gtfsRealtimeVersion;
    }


    public void setGtfsRealtimeVersion(String gtfsRealtimeVersion)
    {
        this.gtfsRealtimeVersion = gtfsRealtimeVersion;
    }

    // incrementality

    public Incrementality getIncrementality()
    {
        return incrementality;
    }


    public void setIncrementality(Incrementality incrementality)
    {
        this.incrementality = incrementality;
    }

    // timestamp

    public Long getTimestamp()
    {
        return timestamp;
    }


    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final FeedHeader that = (FeedHeader) obj;
        return
                Objects.equals(this.gtfsRealtimeVersion, that.gtfsRealtimeVersion) &&
                Objects.equals(this.incrementality, that.incrementality) &&
                Objects.equals(this.timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gtfsRealtimeVersion, incrementality, timestamp);
    }

    @Override
    public String toString() {
        return "FeedHeader{" +
                    "gtfsRealtimeVersion=" + gtfsRealtimeVersion +
                    ", incrementality=" + incrementality +
                    ", timestamp=" + timestamp +
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

    public Schema<FeedHeader> cachedSchema()
    {
        return DEFAULT_INSTANCE;
    }

    // schema methods

    public FeedHeader newMessage()
    {
        return new FeedHeader();
    }

    public Class<FeedHeader> typeClass()
    {
        return FeedHeader.class;
    }

    public String messageName()
    {
        return FeedHeader.class.getSimpleName();
    }

    public String messageFullName()
    {
        return FeedHeader.class.getName();
    }

    public boolean isInitialized(FeedHeader message)
    {
        return 
            message.gtfsRealtimeVersion != null;
    }

    public void mergeFrom(Input input, FeedHeader message) throws IOException
    {
        for(int number = input.readFieldNumber(this);; number = input.readFieldNumber(this))
        {
            switch(number)
            {
                case 0:
                    return;
                case 1:
                    message.gtfsRealtimeVersion = input.readString();
                    break;
                case 2:
                    message.incrementality = Incrementality.valueOf(input.readEnum());
                    break;

                case 3:
                    message.timestamp = input.readUInt64();
                    break;
                default:
                    input.handleUnknownField(number, this);
            }   
        }
    }


    public void writeTo(Output output, FeedHeader message) throws IOException
    {
        if(message.gtfsRealtimeVersion == null)
            throw new UninitializedMessageException(message);
        output.writeString(1, message.gtfsRealtimeVersion, false);

        if(message.incrementality != null && message.incrementality != DEFAULT_INCREMENTALITY)
             output.writeEnum(2, message.incrementality.number, false);

        if(message.timestamp != null)
            output.writeUInt64(3, message.timestamp, false);
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