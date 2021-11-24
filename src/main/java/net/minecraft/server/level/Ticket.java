package net.minecraft.server.level;

import java.util.Objects;

public final class Ticket<T> implements Comparable<Ticket<?>> {
   private final TicketType<T> type;
   private final int ticketLevel;
   private final T key;
   private long createdTick;

   protected Ticket(TicketType<T> p_9425_, int p_9426_, T p_9427_) {
      this.type = p_9425_;
      this.ticketLevel = p_9426_;
      this.key = p_9427_;
   }

   public int compareTo(Ticket<?> p_9432_) {
      int i = Integer.compare(this.ticketLevel, p_9432_.ticketLevel);
      if (i != 0) {
         return i;
      } else {
         int j = Integer.compare(System.identityHashCode(this.type), System.identityHashCode(p_9432_.type));
         return j != 0 ? j : this.type.getComparator().compare(this.key, (T)p_9432_.key);
      }
   }

   public boolean equals(Object p_9439_) {
      if (this == p_9439_) {
         return true;
      } else if (!(p_9439_ instanceof Ticket)) {
         return false;
      } else {
         Ticket<?> ticket = (Ticket)p_9439_;
         return this.ticketLevel == ticket.ticketLevel && Objects.equals(this.type, ticket.type) && Objects.equals(this.key, ticket.key);
      }
   }

   public int hashCode() {
      return Objects.hash(this.type, this.ticketLevel, this.key);
   }

   public String toString() {
      return "Ticket[" + this.type + " " + this.ticketLevel + " (" + this.key + ")] at " + this.createdTick;
   }

   public TicketType<T> getType() {
      return this.type;
   }

   public int getTicketLevel() {
      return this.ticketLevel;
   }

   protected void setCreatedTick(long pTimestamp) {
      this.createdTick = pTimestamp;
   }

   protected boolean timedOut(long pCurrentTime) {
      long i = this.type.timeout();
      return i != 0L && pCurrentTime - this.createdTick > i;
   }
}
