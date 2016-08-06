/*******************************************************************************
 * Copyright 2016 bwsoft and others
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
 *******************************************************************************/
package com.bwsoft.iris.message;

import java.nio.ByteBuffer;

public interface Message extends Group {
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset);
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset, int length);
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset);
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset, int length);
	public GroupObject createSbeBuffer(ByteBuffer buffer, int offset);
}
