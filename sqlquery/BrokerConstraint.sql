use Trading;
go
IF OBJECT_ID ('Trading.brokerCheckTrigger', 'TR') IS NOT NULL
   DROP TRIGGER Trading.brokerCheckTrigger;
GO
Create Trigger brokerCheckTrigger ON Trading.dbo.BrokerTrades After Insert, Update
As
Begin

   If NOT Exists(select broker from brokerList where broker in (Select broker from inserted)) BEGIN
      RAISERROR ('Incorrect Broker Name', 16, 10);
   END

END