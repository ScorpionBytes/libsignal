//
// Copyright 2024 Signal Messenger, LLC.
// SPDX-License-Identifier: AGPL-3.0-only
//

package org.signal.libsignal.messagebackup;

import static org.signal.libsignal.internal.FilterExceptions.filterExceptions;

import org.signal.libsignal.internal.Native;
import org.signal.libsignal.internal.NativeHandleGuard;
import org.signal.libsignal.protocol.ServiceId.Aci;

/**
 * Key used to encrypt and decrypt a message backup bundle.
 *
 * @see BackupKey
 */
public class MessageBackupKey extends NativeHandleGuard.SimpleOwner {

  private MessageBackupKey(long nativeHandle) {
    super(nativeHandle);
  }

  /**
   * @deprecated Use AccountEntropyPool instead.
   */
  @Deprecated
  public MessageBackupKey(byte[] masterKey, Aci aci) {
    super(Native.MessageBackupKey_FromMasterKey(masterKey, aci.toServiceIdFixedWidthBinary()));
  }

  /**
   * Derives a MessageBackupKey from the given account entropy pool.
   *
   * <p>{@code accountEntropy} must be a <b>validated</b> account entropy pool; passing an arbitrary
   * String here is considered a programmer error.
   */
  public MessageBackupKey(String accountEntropy, Aci aci) {
    super(
        Native.MessageBackupKey_FromAccountEntropyPool(
            accountEntropy, aci.toServiceIdFixedWidthBinary()));
  }

  /**
   * Derives a MessageBackupKey from a backup key and ID.
   *
   * <p>Used when reading from a local backup, which may have been created with a different ACI.
   *
   * <p>This uses AccountEntropyPool-based key derivation rules; it cannot be used to read a backup
   * created from a master key.
   */
  public MessageBackupKey(BackupKey backupKey, byte[] backupId) {
    super(
        Native.MessageBackupKey_FromBackupKeyAndBackupId(
            backupKey.getInternalContentsForJNI(), backupId));
  }

  /**
   * @deprecated Use the overload that takes a strongly-typed BackupKey instead.
   */
  @Deprecated
  public MessageBackupKey(byte[] backupKey, byte[] backupId) {
    this(filterExceptions(() -> new BackupKey(backupKey)), backupId);
  }

  /**
   * Constructs a MessageBackupKey from the individual keys that make it up.
   *
   * <p>Will throw an unchecked exception if the keys are the wrong length; you're expected to only
   * use this with keys previously derived by this class (or its equivalent in another language).
   */
  public static MessageBackupKey fromParts(byte[] hmacKey, byte[] aesKey) {
    return new MessageBackupKey(Native.MessageBackupKey_FromParts(hmacKey, aesKey));
  }

  protected void release(long nativeHandle) {
    Native.MessageBackupKey_Destroy(nativeHandle);
  }

  /** An HMAC key used to sign a backup file. */
  public byte[] getHmacKey() {
    return guardedMap(Native::MessageBackupKey_GetHmacKey);
  }

  /** An AES-256-CBC key used to encrypt a backup file. */
  public byte[] getAesKey() {
    return guardedMap(Native::MessageBackupKey_GetAesKey);
  }
}
